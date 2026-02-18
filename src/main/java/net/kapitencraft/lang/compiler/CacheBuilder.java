package net.kapitencraft.lang.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.bytecode.storage.Chunk;
import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.lang.compiler.instruction.SwitchInstruction;
import net.kapitencraft.lang.compiler.instruction.constant.DoubleConstantInstruction;
import net.kapitencraft.lang.compiler.instruction.constant.FloatConstantInstruction;
import net.kapitencraft.lang.holder.LiteralHolder;
import net.kapitencraft.lang.holder.ast.ElifBranch;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.natives.NativeClassInstance;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.RetentionPolicy;
import java.util.*;
import java.util.function.Consumer;

public class CacheBuilder implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    public static final int majorVersion = 1, minorVersion = 0;

    //marks whether to keep the expr result on the stack or not
    private boolean retainExprResult = false;
    //marks whether the expr result has already been ignored and therefore no POP must be emitted
    private boolean ignoredExprResult = false;
    private final CodeBuilder codeBuilder;
    private final Chunk.Builder builder = new Chunk.Builder();
    private final Stack<Loop> loops = new Stack<>();

    public CacheBuilder() {
        this.codeBuilder = new CodeBuilder();
    }

    public void cache(Expr expr) {
        expr.accept(this);
    }

    private void cacheOrNull(@Nullable Expr expr) {
        if (expr == null) {
            builder.addCode(Opcode.NULL);
            codeBuilder.addSimple(Opcode.NULL);
        }
        else cache(expr);
    }

    public void cache(Stmt stmt) {
        stmt.accept(this);
    }

    public void saveArgs(Expr[] args) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        for (Expr arg : args) {
            this.cache(arg);
        }
        retainExprResult = hadRetain;
    }

    public JsonObject cacheClass(CacheableClass loxClass) {
        return loxClass.save(this); //TODO convert to entirely bytecode later
    }

    public JsonArray cacheAnnotations(Annotation[] annotations) {
        JsonArray array = new JsonArray();
        for (Annotation instance : annotations) {
            Annotation retention;
            if ((retention = VarTypeManager.directParseTypeCompiler(instance.getType()).get().getAnnotation(VarTypeManager.RETENTION)) != null) {
                if (((NativeClassInstance) retention.getProperty("value")).getObject() == RetentionPolicy.SOURCE) {
                    continue;
                }
                //TODO create annotation processor
            }
            array.add(instance.toJson());
        }
        return array;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        AssignOperators result = getAssignOperators(expr.ordinal());
        assign(expr.executor(), expr.value(), expr.type(), result.get(), result.assign(), b -> {
            if (expr.ordinal() > 2) b.addArg(expr.ordinal());
        });
        return null;
    }

    private record AssignOperators(Opcode get, Opcode assign) {
    }

    private static @NotNull AssignOperators getAssignOperators(int ordinal) {
        Opcode get = Opcode.GET;
        Opcode assign = Opcode.ASSIGN;
        switch (ordinal) {
            case 0 -> {
                get = Opcode.GET_0;
                assign = Opcode.ASSIGN_0;
            }
            case 1 -> {
                get = Opcode.GET_1;
                assign = Opcode.ASSIGN_1;
            }
            case 2 -> {
                get = Opcode.GET_2;
                assign = Opcode.ASSIGN_2;
            }
        }
        return new AssignOperators(get, assign);
    }

    //TODO enable DUP if `Assign` / `VarDecl` is directly followed by a `Get`
    @Override
    public Void visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        AssignOperators operators = getAssignOperators(expr.ordinal());
        specialAssign(expr.executor(), expr.assignType(), operators.get(), operators.assign(), b -> {
            if (expr.ordinal() > 2) b.addArg(expr.ordinal());
        }, o -> {});
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.right());
        cache(expr.left());
        if (hadRetain) { //if the result of a binary expression is ignored, we don't need to do its calculation as it is pure without side effects
            final ClassReference executor = expr.executor();
            Token operator = expr.operator();
            this.builder.changeLineIfNecessary(operator);
            codeBuilder.changeLineIfNecessary(operator);
            Opcode opcode = switch (operator.type()) {
                case EQUAL -> Opcode.EQUAL;
                case NEQUAL -> Opcode.NEQUAL;
                case LEQUAL -> getLequal(executor);
                case GEQUAL -> getGequal(executor);
                case LESSER -> getLesser(executor);
                case GREATER -> getGreater(executor);
                case SUB -> getSub(executor);
                case ADD -> getAdd(executor);
                case MUL -> getMul(executor);
                case DIV -> getDiv(executor);
                case POW -> getPow(executor);
                default -> throw new IllegalStateException("not a operator: " + operator.type());
            };
            builder.addCode(opcode);
            codeBuilder.addSimple(opcode);
        } else {
            builder.addCode(Opcode.POP_2);
            codeBuilder.addSimple(Opcode.POP_2);
            ignoredExprResult = true;
        }
        retainExprResult = hadRetain;
        return null;
    }

    //region comparison
    private Opcode getGreater(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_GREATER;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_GREATER;
        throw new IllegalStateException("could not create 'greater' for: " + reference);
    }

    private Opcode getLesser(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_LESSER;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_LESSER;
        throw new IllegalStateException("could not create 'lesser' for: " + reference);
    }

    private Opcode getGequal(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_GEQUAL;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_GEQUAL;
        throw new IllegalStateException("could not create 'gequal' for: " + reference);
    }

    private Opcode getLequal(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_LEQUAL;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_LEQUAL;
        throw new IllegalStateException("could not create 'lequal' for: " + reference);
    }

    //endregion

    @Override
    public Void visitWhenExpr(Expr.When expr) {
        cache(expr.condition());
        this.builder.jumpElse(() -> cache(expr.ifTrue()), () -> cache(expr.ifFalse()));
        this.codeBuilder.jumpElse(() -> cache(expr.ifTrue()), () -> cache(expr.ifFalse()));
        return null;
    }

    @Override
    public Void visitInstCallExpr(Expr.InstCall expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.callee());
        this.builder.changeLineIfNecessary(expr.name());
        codeBuilder.changeLineIfNecessary(expr.name());
        saveArgs(expr.args());
        retainExprResult = hadRetain; //object is NOT POPED from the stack. keep it before the args
        builder.invokeVirtual(expr.id());
        codeBuilder.addStringInstruction(Opcode.INVOKE_VIRTUAL, expr.id());
        if (expr.retType().is(VarTypeManager.VOID))
            ignoredExprResult = true;
        return null;
    }

    @Override
    public Void visitStaticCallExpr(Expr.StaticCall expr) {
        builder.changeLineIfNecessary(expr.name());
        codeBuilder.changeLineIfNecessary(expr.name());
        saveArgs(expr.args());
        builder.invokeStatic(expr.id());
        codeBuilder.addStringInstruction(Opcode.INVOKE_STATIC, expr.id());
        if (expr.retType().is(VarTypeManager.VOID))
            ignoredExprResult = true;
        return null;
    }

    //TODO merge?
    @Override
    public Void visitSuperCallExpr(Expr.SuperCall expr) {
        getVar(0);
        builder.changeLineIfNecessary(expr.name());
        codeBuilder.changeLineIfNecessary(expr.name());
        saveArgs(expr.args());
        builder.invokeStatic(expr.id());
        codeBuilder.addStringInstruction(Opcode.INVOKE_STATIC, expr.id());
        if (expr.retType().is(VarTypeManager.VOID))
            ignoredExprResult = true;
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.object());
        if (hadRetain) {
            builder.changeLineIfNecessary(expr.name());
            codeBuilder.changeLineIfNecessary(expr.name());
            if (expr.type().get().isArray()) { //only `.length` exists on arrays, so we can be sure
                builder.addCode(Opcode.ARRAY_LENGTH);
                codeBuilder.addSimple(Opcode.ARRAY_LENGTH);
            } else {
                builder.addCode(Opcode.GET_FIELD);
                codeBuilder.addStringInstruction(Opcode.GET_FIELD, expr.name().lexeme());
                builder.injectString(expr.name().lexeme());
            }
        } else {
            builder.addCode(Opcode.POP);
            codeBuilder.addSimple(Opcode.POP);
            ignoredExprResult = true;
        }
        return null;
    }

    @Override
    public Void visitStaticGetExpr(Expr.StaticGet expr) {
        if (retainExprResult) {
            builder.changeLineIfNecessary(expr.name());
            codeBuilder.changeLineIfNecessary(expr.name());
            builder.addCode(Opcode.GET_STATIC);
            codeBuilder.addSimple(Opcode.GET_STATIC);
            //TODO 2x String
            builder.injectString(VarTypeManager.getClassName(expr.target().get()));
            builder.injectString(expr.name().lexeme());
        }
        return null;
    }

    @Override
    public Void visitArrayGetExpr(Expr.ArrayGet expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.index());
        cache(expr.object());
        if (hadRetain) {
            Opcode opcode = getArrayLoad(expr.type());
            builder.addCode(opcode);
            codeBuilder.addSimple(opcode);
        } else {
            builder.addCode(Opcode.POP_2);
            codeBuilder.addSimple(Opcode.POP_2);
            ignoredExprResult = true;
        }
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {

        ClassReference retType = expr.executor();
        TokenType type = expr.assignType().type();
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.object());
        if (type != TokenType.ASSIGN) {
            builder.addCode(Opcode.DUP); //duplicate object down so the get field
            builder.changeLineIfNecessary(expr.name());
            codeBuilder.changeLineIfNecessary(expr.name());
            builder.addCode(Opcode.GET_FIELD);
            builder.injectString(expr.name().lexeme());
            codeBuilder.addStringInstruction(Opcode.GET_FIELD, expr.name().lexeme());
            cache(expr.value());
            builder.changeLineIfNecessary(expr.assignType());
            codeBuilder.changeLineIfNecessary(expr.assignType());
            Opcode opcode = switch (type) {
                case ADD_ASSIGN -> getAdd(retType);
                case SUB_ASSIGN -> getSub(retType);
                case MUL_ASSIGN -> getMul(retType);
                case DIV_ASSIGN -> getDiv(retType);
                case POW_ASSIGN -> getPow(retType);
                default -> throw new IllegalArgumentException("not a assign type: " + type);
            };
            builder.addCode(opcode);
            codeBuilder.addSimple(opcode);
        } else {
            cache(expr.value());
            builder.changeLineIfNecessary(expr.assignType());
            codeBuilder.changeLineIfNecessary(expr.assignType());
        }
        if (hadRetain) {
            builder.addCode(Opcode.DUP_X1); //duplicate to keep value on the stack
            codeBuilder.addSimple(Opcode.DUP_X1);
        } else {
            ignoredExprResult = true;
        }
        retainExprResult = hadRetain;
        builder.changeLineIfNecessary(expr.name());
        codeBuilder.changeLineIfNecessary(expr.name());
        builder.addCode(Opcode.PUT_FIELD);
        builder.injectString(expr.name().lexeme());
        codeBuilder.addStringInstruction(Opcode.PUT_FIELD, expr.name().lexeme());
        return null;
    }

    @Override
    public Void visitStaticSetExpr(Expr.StaticSet expr) {
        String className = VarTypeManager.getClassName(expr.target().get());
        String fieldName = expr.name().lexeme();
        assign(expr.executor(), expr.value(), expr.assignType(), Opcode.GET_STATIC, Opcode.PUT_STATIC, b -> {
            b.injectString(className);
            b.injectString(fieldName);
        });

        return null;
    }

    @Override
    public Void visitArraySetExpr(Expr.ArraySet expr) {
        //order: arr, index, val -> val
        ClassReference retType = expr.executor();
        TokenType type = expr.assignType().type();
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        if (type != TokenType.ASSIGN) {
            cache(expr.value());
            cache(expr.object());
            cache(expr.index());
            builder.addCode(Opcode.DUP2_X1);
            codeBuilder.addSimple(Opcode.DUP2_X1);
            Opcode load = getArrayLoad(retType);
            builder.addCode(load);
            codeBuilder.addSimple(load);
            builder.changeLineIfNecessary(expr.assignType());
            codeBuilder.changeLineIfNecessary(expr.assignType());
            Opcode opcode = switch (type) {
                case ADD_ASSIGN -> getAdd(retType);
                case SUB_ASSIGN -> getSub(retType);
                case MUL_ASSIGN -> getMul(retType);
                case DIV_ASSIGN -> getDiv(retType);
                case POW_ASSIGN -> getPow(retType);
                default -> throw new IllegalStateException("unknown assign type: " + type);
            };
            builder.addCode(opcode);
            codeBuilder.addSimple(opcode);
        } else {
            cache(expr.object());
            cache(expr.index());
            cache(expr.value());
        }
        if (hadRetain) {
            builder.addCode(Opcode.DUP); //duplicate to keep the value on the stack as the ARRAY_SET does not actually keep anything on the stack
            codeBuilder.addSimple(Opcode.DUP);
        }
        else
            ignoredExprResult = true;
        retainExprResult = hadRetain;
        Opcode store = getArrayStore(retType);
        builder.addCode(store);
        codeBuilder.addSimple(store);
        return null;
    }

    @Override
    public Void visitSpecialSetExpr(Expr.SpecialSet expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.callee());
        retainExprResult = hadRetain;
        specialAssign(expr.retType(), expr.assignType(), Opcode.GET_FIELD, Opcode.PUT_FIELD,
                b -> b.injectString(expr.name().lexeme()),
                o -> codeBuilder.addStringInstruction(o, expr.name().lexeme())
        );
        return null;
    }

    @Override
    public Void visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        String id = VarTypeManager.getClassName(expr.target().get());

        ClassReference reference = expr.executor();
        builder.addCode(expr.assignType().type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference)
        );

        specialAssign(expr.executor(), expr.assignType(), Opcode.GET_STATIC, Opcode.PUT_STATIC,
                b -> b.injectString(id),
                o -> codeBuilder.addStringInstruction(o, id)
        );
        return null;
    }

    @Override
    public Void visitArraySpecialExpr(Expr.ArraySpecial expr) {
        ClassReference reference = expr.executor();
        builder.changeLineIfNecessary(expr.assignType());
        codeBuilder.changeLineIfNecessary(expr.assignType());
        Opcode opcode = expr.assignType().type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference);
        builder.addCode(opcode);
        codeBuilder.addSimple(opcode);
        Opcode add = getAdd(reference);
        builder.addCode(add);
        codeBuilder.addSimple(add);
        cache(expr.index());
        cache(expr.object());
        builder.addCode(Opcode.DUP2_X1);
        codeBuilder.addSimple(Opcode.DUP2_X1);
        return null;
    }

    private void specialAssign(ClassReference reference, Token token, Opcode get, Opcode set, Consumer<Chunk.Builder> meta, Consumer<Opcode> instructionSink) {
        builder.addCode(get);
        meta.accept(builder);
        instructionSink.accept(get);
        builder.changeLineIfNecessary(token);
        Opcode o = token.type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference);
        builder.addCode(o);
        codeBuilder.addSimple(o);
        Opcode add = getAdd(reference);
        builder.addCode(add);
        codeBuilder.addSimple(add);
        if (retainExprResult) {
            builder.addCode(Opcode.DUP); //duplicate value to emit it onto the object stack
            codeBuilder.addSimple(Opcode.DUP);
        }
        else
            ignoredExprResult = true;
        builder.addCode(set);
        codeBuilder.addSimple(set);
        meta.accept(builder);
        instructionSink.accept(set);
    }

    //region special assign
    private Opcode getMinusOne(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_M1;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_M1;
        throw new IllegalStateException();
    }

    private Opcode getPlusOne(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_1;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_1;
        throw new IllegalStateException("");
    }
    //endregion

    @Override
    public Void visitSliceExpr(Expr.Slice expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.object());
        cacheOrNull(expr.start());
        cacheOrNull(expr.end());
        cacheOrNull(expr.interval());
        builder.addCode(Opcode.SLICE);
        codeBuilder.addSimple(Opcode.SLICE);
        retainExprResult = hadRetain;
        return null;
    }

    @Override
    public Void visitSwitchExpr(Expr.Switch expr) {
        cache(expr.provider());
        builder.addCode(Opcode.SWITCH);
        int defaultPatch = builder.currentCodeIndex();
        int instDefaultPatch = codeBuilder.size();
        builder.addArg(0);
        builder.addArg(0);
        builder.add2bArg(expr.params().size()); //length of pairs

        //compile entries to add sorted
        List<Integer> keys = new ArrayList<>(expr.params().keySet());
        keys.sort(Integer::compareTo);
        record SwitchEntry(int key, int opcode, Expr entry) {}

        List<SwitchEntry> entries = new ArrayList<>();
        List<SwitchInstruction.Entry> instEntries = new ArrayList<>();
        for (Integer key : keys) {
            Expr expr1 = expr.params().get(key);
            builder.add4bArg(key);
            entries.add(new SwitchEntry(key, builder.currentCodeIndex(), expr1));
            builder.addArg(0);
            builder.addArg(0);
            instEntries.add(new SwitchInstruction.Entry(key));
        }
        codeBuilder.addSwitch(expr.params().size(), instEntries);
        List<Integer> continueJumps = new ArrayList<>();
        List<Integer> continueJumpInstructions = new ArrayList<>();

        //cache entries
        for (int i = 0, entriesSize = entries.size(); i < entriesSize; i++) {
            SwitchEntry entry = entries.get(i);
            builder.patchJumpCurrent(entry.opcode);
            instEntries.get(i).setIdx(codeBuilder.size());
            cache(entry.entry);
            if (expr.defaulted() != null || i < entriesSize - 1) {
                continueJumps.add(builder.addJump());
                continueJumpInstructions.add(codeBuilder.addJump());
            }
        }
        builder.patchJumpCurrent(defaultPatch);
        codeBuilder.patchJump(instDefaultPatch);
        if (expr.defaulted() != null) {
            cache(expr.defaulted());
        }

        continueJumps.forEach(builder::patchJumpCurrent);
        codeBuilder.addJumpMultiTargetInstruction(continueJumpInstructions);
        //https://docs.oracle.com/javase/specs/jvms/se25/html/jvms-6.html#jvms-6.5.lookupswitch
        return null;
    }

    @Override
    public Void visitCastCheckExpr(Expr.CastCheck expr) {
        //TODO
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        cache(expr.expression());
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        if (!retainExprResult) {
            ignoredExprResult = true;
            return null;
        }
        Token literalToken = expr.literal();
        builder.changeLineIfNecessary(literalToken);
        codeBuilder.changeLineIfNecessary(literalToken);
        LiteralHolder literal = literalToken.literal();
        ScriptedClass scriptedClass = literal.type();
        Object value = literal.value();
        if (scriptedClass == VarTypeManager.DOUBLE) {
            double v = (double) value;
            if (v == 1d) {
                builder.addCode(Opcode.D_1);
                codeBuilder.addSimple(Opcode.D_1);
            }
            else if (v == -1d) {
                builder.addCode(Opcode.D_M1);
                codeBuilder.addSimple(Opcode.D_M1);
            } else {
                builder.addDoubleConstant(v);
                codeBuilder.add(new DoubleConstantInstruction(v));
            }
        } else if (scriptedClass == VarTypeManager.INTEGER) {
            int v = (int) value;
            builder.addInt(v);
            codeBuilder.addInt(v);
        } else if (VarTypeManager.STRING.is(scriptedClass)) {
            builder.addStringConstant((String) value);
            codeBuilder.addStringInstruction(Opcode.S_CONST, ((String) value));
        } else if (VarTypeManager.FLOAT.is(scriptedClass)) {
            float v = (float) value;
            if (v == 1f) {
                builder.addCode(Opcode.F_1);
                codeBuilder.addSimple(Opcode.F_1);
            } else if (v == -1f) {
                builder.addCode(Opcode.F_M1);
                codeBuilder.addSimple(Opcode.F_M1);
            } else {
                builder.addFloatConstant(v);
                codeBuilder.add(new FloatConstantInstruction(v));
            }
        }
        return null;
    }

    @Override
    public Void visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        if (expr.size() != null) {
            cache(expr.size());
        } else {
            builder.addIntConstant(expr.obj().length);
            //TODO
        }
        builder.changeLineIfNecessary(expr.keyword());
        codeBuilder.changeLineIfNecessary(expr.keyword());
        Opcode opcode = getArrayNew(expr.compoundType());
        builder.addCode(opcode);
        codeBuilder.addSimple(opcode);
        //builder.injectString(VarTypeManager.getClassName(expr.compoundType().get()));
        Expr[] objects = expr.obj();
        Opcode store = getArrayStore(expr.compoundType());
        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                builder.addCode(Opcode.DUP);
                codeBuilder.addSimple(Opcode.DUP);
                //TODO
                builder.addInt(i);
                codeBuilder.addInt(i);
                cache(objects[i]);
                builder.addCode(store);
                codeBuilder.addSimple(store);
            }
        }
        retainExprResult = hadRetain;
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.right());
        cache(expr.left());
        if (hadRetain) {
            builder.changeLineIfNecessary(expr.operator());
            codeBuilder.changeLineIfNecessary(expr.operator());
            Opcode opcode = switch (expr.operator().type()) {
                case OR -> Opcode.OR;
                case XOR -> Opcode.XOR;
                case AND -> Opcode.AND;
                default -> throw new IllegalArgumentException("unknown logical type: " + expr.operator());
            };
            builder.addCode(opcode);
            codeBuilder.addSimple(opcode);
        } else {
            builder.addCode(Opcode.POP_2);
            codeBuilder.addSimple(Opcode.POP_2);
            ignoredExprResult = true;
        }
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.right());
        if (hadRetain) {
            Token operator = expr.operator();
            builder.changeLineIfNecessary(operator);
            codeBuilder.changeLineIfNecessary(operator);
            if (operator.type() == TokenType.NOT) {
                builder.addCode(Opcode.NOT);
                codeBuilder.addSimple(Opcode.NOT);
            }
            else {
                Opcode neg = getNeg(expr.executor());
                builder.addCode(neg);
                codeBuilder.addSimple(neg);
            }
        }
        retainExprResult = hadRetain;
        return null;
    }

    @Override
    public Void visitVarRefExpr(Expr.VarRef expr) {
        if (retainExprResult) {
            builder.changeLineIfNecessary(expr.name());
            codeBuilder.changeLineIfNecessary(expr.name());
            getVar(expr.ordinal());
        } else
            ignoredExprResult = true;
        return null;
    }

    @Override
    public Void visitConstructorExpr(Expr.Constructor expr) {
        builder.changeLineIfNecessary(expr.keyword());
        builder.addCode(Opcode.NEW);
        ScriptedClass target = expr.target().get();
        builder.injectString(VarTypeManager.getClassName(target));

        if (expr.signature() != null) {
            if (retainExprResult) {
                builder.addCode(Opcode.DUP);
            } else {
                ignoredExprResult = true;
            }
            saveArgs(expr.args());
            builder.invokeVirtual(expr.signature());
        }

        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        for (Stmt statement : stmt.statements()) {
            cache(statement);
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        retainExprResult = false;
        ignoredExprResult = false;
        cache(stmt.expression());
        if (!ignoredExprResult)
            builder.addCode(Opcode.POP);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "if");
        retainExprResult = true;
        builder.changeLineIfNecessary(stmt.keyword());
        codeBuilder.changeLineIfNecessary(stmt.keyword());
        cache(stmt.condition());
        int jumpPatch = builder.addJumpIfFalse();
        int instPatch = codeBuilder.addJumpIfFalse();
        retainExprResult = false;
        cache(stmt.thenBranch());
        if (stmt.elifs().length > 0 || stmt.elseBranch() != null) {
            List<Integer> branches = new ArrayList<>();
            List<Integer> instBranches = new ArrayList<>();
            branches.add(builder.addJump()); //jump from branch past the IF
            for (int i = 0; i < stmt.elifs().length; i++) {
                builder.patchJumpCurrent(jumpPatch);
                codeBuilder.patchJump(instPatch);
                ElifBranch branch = stmt.elifs()[i];
                cache(branch.condition());
                jumpPatch = builder.addJumpIfFalse();
                instPatch = codeBuilder.addJumpIfFalse();
                retainExprResult = false;
                cache(branch.body());
                if (!branch.seenReturn()) {
                    branches.add(builder.addJump());
                    instBranches.add(codeBuilder.addJump());
                }
            }
            if (stmt.elseBranch() != null) {
                builder.patchJumpCurrent(jumpPatch);
                codeBuilder.patchJump(instPatch);
                retainExprResult = false;
                cache(stmt.elseBranch());
            }
            for (int branch : branches) {
                builder.patchJumpCurrent(branch);
            }
            codeBuilder.addJumpMultiTargetInstruction(instBranches);
        } else {
            builder.patchJumpCurrent(jumpPatch);
            codeBuilder.patchJump(instPatch);
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value() != null) {
            retainExprResult = true;
            builder.changeLineIfNecessary(stmt.keyword());
            codeBuilder.changeLineIfNecessary(stmt.keyword());
            cache(stmt.value());
            builder.addCode(Opcode.RETURN_ARG);
            codeBuilder.addSimple(Opcode.RETURN_ARG);
        } else {
            builder.addCode(Opcode.RETURN);
            codeBuilder.addSimple(Opcode.RETURN);
        }
        return null;
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        retainExprResult = true;
        cache(stmt.value());
        builder.changeLineIfNecessary(stmt.keyword());
        builder.addCode(Opcode.THROW);
        return null;
    }

    @Override
    public Void visitVarDeclStmt(Stmt.VarDecl stmt) {
        retainExprResult = true;
        builder.changeLineIfNecessary(stmt.name());
        cacheOrNull(stmt.initializer()); //adding a value to the stack without removing it automatically adds it as a local variable
        builder.addLocal(builder.currentCodeIndex(), stmt.localId(), stmt.type(), stmt.name().lexeme());
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        int index = builder.currentCodeIndex();
        retainExprResult = true;
        builder.changeLineIfNecessary(stmt.keyword());
        cache(stmt.condition());
        int skip = builder.addJumpIfFalse();
        loops.add(new Loop((short) index));
        retainExprResult = false;
        cache(stmt.body());
        int returnIndex = builder.addJump();
        loops.pop().patchBreaks();
        builder.patchJumpCurrent(skip);
        builder.patchJump(returnIndex, (short) index);
        return null;
    }

    //clears locals off the stack when they move out of scope
    @Override
    public Void visitClearLocalsStmt(Stmt.ClearLocals stmt) {
        int amount = stmt.amount();
        while (amount >= 2) {
            builder.addCode(Opcode.POP_2);
            amount -= 2;
        }
        if (amount > 0)
            builder.addCode(Opcode.POP);
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        builder.changeLineIfNecessary(stmt.keyword());
        cache(stmt.init());
        int result = builder.currentCodeIndex();
        retainExprResult = true;
        ignoredExprResult = false;
        cache(stmt.condition());
        int jump1 = builder.addJumpIfFalse();
        loops.add(new Loop((short) result));
        retainExprResult = false;
        ignoredExprResult = false;
        cache(stmt.body());
        retainExprResult = false;
        ignoredExprResult = false;
        cache(stmt.increment());
        if (!ignoredExprResult)
            builder.addCode(Opcode.POP); //pop the result of the increment
        int returnIndex = builder.addJump();
        loops.pop().patchBreaks();
        builder.patchJumpCurrent(jump1);
        builder.patchJump(returnIndex, (short) result);
        return null;
    }

    @Override
    public Void visitForEachStmt(Stmt.ForEach stmt) {
        builder.addLocal(builder.currentCodeIndex(), stmt.baseVar() + 1, stmt.type(), stmt.name().lexeme());
        retainExprResult = true;
        builder.changeLineIfNecessary(stmt.name());
        cache(stmt.initializer()); //create array variable
        builder.addCode(Opcode.I_0); //create iteration variable
        int baseVarIndex = stmt.baseVar();

        int curIndex = builder.currentCodeIndex(); //link to jump back when loop is completed

        //region condition
        getVar(baseVarIndex); //get array var
        builder.addCode(Opcode.ARRAY_LENGTH); //get length of array
        getVar(baseVarIndex + 1); //get iteration var
        builder.addCode(Opcode.I_LESSER); //check if iteration var is less than the length of the array
        int result = builder.addJumpIfFalse(); //create jump out of the loop if check fails
        //endregion
        loops.add(new Loop((short) result)); //push loop

        //region load iteration object
        getVar(baseVarIndex + 1); //load iteration var
        getVar(baseVarIndex); //load array var
        builder.addCode(getArrayLoad(stmt.type()));  //create entry var by loading array element
        //endregion

        retainExprResult = false;
        cache(stmt.body()); //cache loop body

        //region increase iteration var
        builder.addCode(Opcode.I_1); //load 1
        getVar(baseVarIndex + 1); //get iteration var
        builder.addCode(Opcode.I_ADD); //add 1 to the iteration var
        assignVar(baseVarIndex + 1);
        //endregion
        int returnIndex = builder.addJump();
        loops.pop().patchBreaks();
        builder.patchJumpCurrent(result);
        builder.patchJump(returnIndex, (short) curIndex);
        return null;
    }

    @Override
    public Void visitDebugTraceStmt(Stmt.DebugTrace stmt) {
        builder.addTraceDebug(stmt.locals());
        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        builder.changeLineIfNecessary(stmt.type());
        Loop loop = loops.peek();
        switch (stmt.type().type()) {
            case BREAK -> loop.addBreak(builder.addJump());
            case CONTINUE -> builder.patchJump(builder.addJump(), loop.condition);
        }
        return null;
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        int handlerStart = builder.currentCodeIndex();
        retainExprResult = false;
        cache(stmt.body());
        int handlerEnd = builder.currentCodeIndex();
        List<Integer> jumps = new ArrayList<>();
        jumps.add(builder.addJump());
        for (Pair<Pair<ClassReference[], Token>, Stmt.Block> aCatch : stmt.catches()) {
            for (ClassReference reference : aCatch.left().left()) {
                builder.addExceptionHandler(handlerStart, handlerEnd, builder.currentCodeIndex(), builder.injectStringNoArg(VarTypeManager.getClassName(reference.get())));
            }
            retainExprResult = false;
            cache(aCatch.right());
            jumps.add(builder.addJump());
        }
        if (stmt.finale() != null) {
            builder.addExceptionHandler(handlerStart, handlerEnd, builder.currentCodeIndex(), 0);
            retainExprResult = false;
            cache(stmt.finale());
        }
        jumps.forEach(builder::patchJumpCurrent);

        //TODO add https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.10
        //also read this: https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.3
        return null;
    }

    private void assignVar(int i) {
        switch (i) { //save the iteration var
            case 0 -> builder.addCode(Opcode.ASSIGN_0);
            case 1 -> builder.addCode(Opcode.ASSIGN_1);
            case 2 -> builder.addCode(Opcode.ASSIGN_2);
            default -> {
                builder.addCode(Opcode.ASSIGN);
                builder.addArg(i);
            }
        }
    }

    private void getVar(int i) {

        switch (i) {
            case 0 -> {
                builder.addCode(Opcode.GET_0);
                codeBuilder.addSimple(Opcode.GET_0);
            }
            case 1 -> {
                builder.addCode(Opcode.GET_1);
                codeBuilder.addSimple(Opcode.GET_1);
            }
            case 2 -> {
                builder.addCode(Opcode.GET_2);
                codeBuilder.addSimple(Opcode.GET_2);
            }
            default -> {
                builder.addCode(Opcode.GET);
                builder.addArg(i);
                codeBuilder.addGet(i);
            }
        }

    }

    public Chunk.Builder setup() {
        this.builder.clear();
        return this.builder;
    }

    //order: idx, arr -> val
    private Opcode getArrayLoad(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.IA_LOAD;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.DA_LOAD;
        if (reference.is(VarTypeManager.CHAR)) return Opcode.CA_LOAD;
        return Opcode.RA_LOAD;
    }

    private Opcode getArrayStore(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.IA_STORE;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.DA_STORE;
        if (reference.is(VarTypeManager.CHAR)) return Opcode.CA_STORE;
        return Opcode.RA_STORE;
    }

    private Opcode getArrayNew(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.IA_NEW;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.DA_NEW;
        if (reference.is(VarTypeManager.CHAR)) return Opcode.CA_NEW;
        return Opcode.RA_NEW;
    }

    private Opcode getDiv(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_DIV;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_DIV;
        throw new IllegalStateException("could not create 'div' for: " + reference);
    }

    private Opcode getMul(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_MUL;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_MUL;
        throw new IllegalStateException("could not create 'mul' for: " + reference);
    }

    private Opcode getSub(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_SUB;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_SUB;
        throw new IllegalStateException("could not create 'sub' for: " + reference);
    }

    private Opcode getAdd(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_ADD;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_ADD;
        if (reference.is(VarTypeManager.STRING.get())) return Opcode.CONCENTRATION;
        throw new IllegalStateException("could not create 'add' for: " + reference);
    }

    private Opcode getPow(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_POW;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_POW;
        throw new IllegalStateException("could not create 'pow' for: " + reference);
    }

    private Opcode getNeg(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.I_NEGATION;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.D_NEGATION;
        throw new IllegalStateException("could not create 'negation' for: " + reference);
    }

    //TODO
    private void assign(ClassReference retType, Expr value, Token type, Opcode get, Opcode assign, Consumer<Chunk.Builder> meta) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(value);
        builder.changeLineIfNecessary(type);
        codeBuilder.changeLineIfNecessary(type);
        if (type.type() != TokenType.ASSIGN) {
            builder.addCode(get);
            meta.accept(builder);
            Opcode operation = switch (type.type()) {
                case ADD_ASSIGN -> getAdd(retType);
                case SUB_ASSIGN -> getSub(retType);
                case MUL_ASSIGN -> getMul(retType);
                case DIV_ASSIGN -> getDiv(retType);
                case POW_ASSIGN -> getPow(retType);
                default -> throw new IllegalStateException("no operation for type: " + type.type());
            };
            builder.addCode(operation);
            codeBuilder.addSimple(operation);
        }
        if (hadRetain) {
            builder.addCode(Opcode.DUP);
            codeBuilder.addSimple(Opcode.DUP);
        } else
            ignoredExprResult = true;
        retainExprResult = hadRetain;
        builder.addCode(assign);
        codeBuilder.addSimple(assign);
        meta.accept(builder);
    }

    private final class Loop {
        private final short condition;
        private final List<Integer> breakIndices;

        private Loop(short condition) {
            this.condition = condition;
            this.breakIndices = new ArrayList<>();
        }

        public void addBreak(int patchIndex) {
            this.breakIndices.add(patchIndex);
        }

        public void patchBreaks() {
            this.breakIndices.forEach(builder::patchJumpCurrent);
        }
    }
}
