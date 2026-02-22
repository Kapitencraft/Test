package net.kapitencraft.lang.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.lang.compiler.instruction.SwitchInstruction;
import net.kapitencraft.lang.compiler.instruction.TraceDebugInstruction;
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
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.natives.NativeClassInstance;
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
    private final ByteCodeBuilder byteCodeBuilder;
    private final Stack<Loop> loops = new Stack<>();

    public CacheBuilder() {
        this.byteCodeBuilder = new ByteCodeBuilder();
    }

    public void cache(Expr expr) {
        expr.accept(this);
    }

    private void cacheOrNull(@Nullable Expr expr) {
        if (expr == null) {
            byteCodeBuilder.addSimple(Opcode.NULL);
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
        byte ordinal = expr.ordinal();
        AssignOperators result = getAssignOperators(ordinal);
        assign(expr.executor(), expr.value(), expr.type(), result.get(), result.assign(), opcode -> {
            if (ordinal > 2)
                byteCodeBuilder.addLocalAccess(opcode, ordinal);
            else
                byteCodeBuilder.addSimple(opcode);
        });
        return null;
    }

    public void build(Chunk.Builder chunk) {
        ByteCodeBuilder.IpContainer container = this.byteCodeBuilder.gatherStartIndexes();
        this.byteCodeBuilder.build(chunk, container);
    }

    public void reset() {
        this.byteCodeBuilder.reset();
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
        int ordinal = expr.ordinal();
        AssignOperators operators = getAssignOperators(ordinal);
        specialAssign(expr.executor(), expr.assignType(), operators.get(), operators.assign(), b -> {
            if (ordinal > 2) b.addArg(ordinal);
        }, o -> {
            if (ordinal > 2)
                byteCodeBuilder.addLocalAccess(o, ordinal);
            else
                byteCodeBuilder.addSimple(o);
        });
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
            byteCodeBuilder.changeLineIfNecessary(operator);
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
            byteCodeBuilder.addSimple(opcode);
        } else {
            byteCodeBuilder.addSimple(Opcode.POP_2);
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
        this.byteCodeBuilder.jumpElse(() -> cache(expr.ifTrue()), () -> cache(expr.ifFalse()));
        return null;
    }

    @Override
    public Void visitInstCallExpr(Expr.InstCall expr) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(expr.callee());
        byteCodeBuilder.changeLineIfNecessary(expr.name());
        saveArgs(expr.args());
        retainExprResult = hadRetain; //object is NOT POPED from the stack. keep it before the args
        byteCodeBuilder.addStringInstruction(Opcode.INVOKE_VIRTUAL, expr.id());
        if (expr.retType().is(VarTypeManager.VOID))
            ignoredExprResult = true;
        return null;
    }

    @Override
    public Void visitStaticCallExpr(Expr.StaticCall expr) {
        byteCodeBuilder.changeLineIfNecessary(expr.name());
        saveArgs(expr.args());
        byteCodeBuilder.addStringInstruction(Opcode.INVOKE_STATIC, expr.id());
        if (expr.retType().is(VarTypeManager.VOID))
            ignoredExprResult = true;
        return null;
    }

    //TODO merge?
    @Override
    public Void visitSuperCallExpr(Expr.SuperCall expr) {
        getVar(0);
        byteCodeBuilder.changeLineIfNecessary(expr.name());
        saveArgs(expr.args());
        byteCodeBuilder.addStringInstruction(Opcode.INVOKE_STATIC, expr.id());
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
            byteCodeBuilder.changeLineIfNecessary(expr.name());
            if (expr.type().get().isArray()) { //only `.length` exists on arrays, so we can be sure
                byteCodeBuilder.addSimple(Opcode.ARRAY_LENGTH);
            } else {
                byteCodeBuilder.addStringInstruction(Opcode.GET_FIELD, expr.name().lexeme());
            }
        } else {
            byteCodeBuilder.addSimple(Opcode.POP);
            ignoredExprResult = true;
        }
        return null;
    }

    @Override
    public Void visitStaticGetExpr(Expr.StaticGet expr) {
        if (retainExprResult) {
            byteCodeBuilder.changeLineIfNecessary(expr.name());
            String className = VarTypeManager.getClassName(expr.target().get());
            String fieldName = expr.name().lexeme();
            byteCodeBuilder.addStaticFieldAccess(Opcode.GET_STATIC, className, fieldName);
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
            byteCodeBuilder.addSimple(opcode);
        } else {
            byteCodeBuilder.addSimple(Opcode.POP_2);
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
            byteCodeBuilder.addSimple(Opcode.DUP);
            byteCodeBuilder.changeLineIfNecessary(expr.name());
            byteCodeBuilder.addStringInstruction(Opcode.GET_FIELD, expr.name().lexeme());
            cache(expr.value());
            byteCodeBuilder.changeLineIfNecessary(expr.assignType());
            Opcode opcode = switch (type) {
                case ADD_ASSIGN -> getAdd(retType);
                case SUB_ASSIGN -> getSub(retType);
                case MUL_ASSIGN -> getMul(retType);
                case DIV_ASSIGN -> getDiv(retType);
                case POW_ASSIGN -> getPow(retType);
                default -> throw new IllegalArgumentException("not a assign type: " + type);
            };
            byteCodeBuilder.addSimple(opcode);
        } else {
            cache(expr.value());
            byteCodeBuilder.changeLineIfNecessary(expr.assignType());
        }
        if (hadRetain) {
            byteCodeBuilder.addSimple(Opcode.DUP_X1); //duplicate to keep value on the stack
        } else {
            ignoredExprResult = true;
        }
        retainExprResult = hadRetain;
        byteCodeBuilder.changeLineIfNecessary(expr.name());
        byteCodeBuilder.addStringInstruction(Opcode.PUT_FIELD, expr.name().lexeme());
        return null;
    }

    @Override
    public Void visitStaticSetExpr(Expr.StaticSet expr) {
        String className = VarTypeManager.getClassName(expr.target().get());
        String fieldName = expr.name().lexeme();
        assign(expr.executor(), expr.value(), expr.assignType(), Opcode.GET_STATIC, Opcode.PUT_STATIC, opcode -> byteCodeBuilder.addStaticFieldAccess(opcode, className, fieldName));

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
            byteCodeBuilder.addSimple(Opcode.DUP2_X1);
            Opcode load = getArrayLoad(retType);
            byteCodeBuilder.addSimple(load);
            byteCodeBuilder.changeLineIfNecessary(expr.assignType());
            Opcode opcode = switch (type) {
                case ADD_ASSIGN -> getAdd(retType);
                case SUB_ASSIGN -> getSub(retType);
                case MUL_ASSIGN -> getMul(retType);
                case DIV_ASSIGN -> getDiv(retType);
                case POW_ASSIGN -> getPow(retType);
                default -> throw new IllegalStateException("unknown assign type: " + type);
            };
            byteCodeBuilder.addSimple(opcode);
        } else {
            cache(expr.object());
            cache(expr.index());
            cache(expr.value());
        }
        if (hadRetain) {
            byteCodeBuilder.addSimple(Opcode.DUP); //duplicate to keep the value on the stack as the ARRAY_SET does not actually keep anything on the stack
        }
        else
            ignoredExprResult = true;
        retainExprResult = hadRetain;
        Opcode store = getArrayStore(retType);
        byteCodeBuilder.addSimple(store);
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
                o -> byteCodeBuilder.addStringInstruction(o, expr.name().lexeme())
        );
        return null;
    }

    @Override
    public Void visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        String id = VarTypeManager.getClassName(expr.target().get());

        ClassReference reference = expr.executor();
        byteCodeBuilder.addSimple(expr.assignType().type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference));

        specialAssign(expr.executor(), expr.assignType(), Opcode.GET_STATIC, Opcode.PUT_STATIC,
                b -> b.injectString(id),
                o -> byteCodeBuilder.addStringInstruction(o, id)
        );
        return null;
    }

    @Override
    public Void visitArraySpecialExpr(Expr.ArraySpecial expr) {
        ClassReference reference = expr.executor();
        byteCodeBuilder.changeLineIfNecessary(expr.assignType());
        Opcode opcode = expr.assignType().type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference);
        byteCodeBuilder.addSimple(opcode);
        Opcode add = getAdd(reference);
        byteCodeBuilder.addSimple(add);
        cache(expr.index());
        cache(expr.object());
        byteCodeBuilder.addSimple(Opcode.DUP2_X1);
        return null;
    }

    private void specialAssign(ClassReference reference, Token token, Opcode get, Opcode set, Consumer<Chunk.Builder> meta, Consumer<Opcode> instructionSink) {
        instructionSink.accept(get);
        byteCodeBuilder.changeLineIfNecessary(token);
        Opcode o = token.type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference);
        byteCodeBuilder.addSimple(o);
        Opcode add = getAdd(reference);
        byteCodeBuilder.addSimple(add);
        if (retainExprResult) {
            byteCodeBuilder.addSimple(Opcode.DUP); //duplicate value to emit it onto the object stack
        } else
            ignoredExprResult = true;
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
        byteCodeBuilder.addSimple(Opcode.SLICE);
        retainExprResult = hadRetain;
        return null;
    }

    @Override
    public Void visitSwitchExpr(Expr.Switch expr) {
        cache(expr.provider());
        int instDefaultPatch = byteCodeBuilder.size();

        //compile entries to add sorted
        List<Integer> keys = new ArrayList<>(expr.params().keySet());
        keys.sort(Integer::compareTo);
        record SwitchEntry(int key, Expr entry) {}

        List<SwitchEntry> entries = new ArrayList<>();
        List<SwitchInstruction.Entry> instEntries = new ArrayList<>();
        for (Integer key : keys) {
            Expr expr1 = expr.params().get(key);
            entries.add(new SwitchEntry(key, expr1));
            instEntries.add(new SwitchInstruction.Entry(key));
        }
        byteCodeBuilder.addSwitch(expr.params().size(), instEntries);
        List<Integer> continueJumpInstructions = new ArrayList<>();

        //cache entries
        for (int i = 0, entriesSize = entries.size(); i < entriesSize; i++) {
            SwitchEntry entry = entries.get(i);
            instEntries.get(i).setIdx(byteCodeBuilder.size());
            cache(entry.entry);
            if (expr.defaulted() != null || i < entriesSize - 1) {
                continueJumpInstructions.add(byteCodeBuilder.addJump());
            }
        }
        byteCodeBuilder.patchJump(instDefaultPatch);
        if (expr.defaulted() != null) {
            cache(expr.defaulted());
        }

        byteCodeBuilder.addJumpMultiTargetInstruction(continueJumpInstructions);
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
        byteCodeBuilder.changeLineIfNecessary(literalToken);
        LiteralHolder literal = literalToken.literal();
        ScriptedClass scriptedClass = literal.type();
        Object value = literal.value();
        if (scriptedClass == VarTypeManager.DOUBLE) {
            double v = (double) value;
            if (v == 1d) {
                byteCodeBuilder.addSimple(Opcode.D_1);
            }
            else if (v == -1d) {
                byteCodeBuilder.addSimple(Opcode.D_M1);
            } else {
                byteCodeBuilder.add(new DoubleConstantInstruction(v));
            }
        } else if (scriptedClass == VarTypeManager.INTEGER) {
            int v = (int) value;
            byteCodeBuilder.addInt(v);
        } else if (VarTypeManager.STRING.is(scriptedClass)) {
            byteCodeBuilder.addStringInstruction(Opcode.S_CONST, ((String) value));
        } else if (VarTypeManager.FLOAT.is(scriptedClass)) {
            float v = (float) value;
            if (v == 1f) {
                byteCodeBuilder.addSimple(Opcode.F_1);
            } else if (v == -1f) {
                byteCodeBuilder.addSimple(Opcode.F_M1);
            } else {
                byteCodeBuilder.add(new FloatConstantInstruction(v));
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
            byteCodeBuilder.addInt(expr.obj().length);
        }
        byteCodeBuilder.changeLineIfNecessary(expr.keyword());
        Opcode opcode = getArrayNew(expr.compoundType());
        byteCodeBuilder.addSimple(opcode);
        //builder.injectString(VarTypeManager.getClassName(expr.compoundType().get()));
        Expr[] objects = expr.obj();
        Opcode store = getArrayStore(expr.compoundType());
        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                byteCodeBuilder.addSimple(Opcode.DUP);
                byteCodeBuilder.addInt(i);
                cache(objects[i]);
                byteCodeBuilder.addSimple(store);
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
            byteCodeBuilder.changeLineIfNecessary(expr.operator());
            Opcode opcode = switch (expr.operator().type()) {
                case OR -> Opcode.OR;
                case XOR -> Opcode.XOR;
                case AND -> Opcode.AND;
                default -> throw new IllegalArgumentException("unknown logical type: " + expr.operator());
            };
            byteCodeBuilder.addSimple(opcode);
        } else {
            byteCodeBuilder.addSimple(Opcode.POP_2);
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
            byteCodeBuilder.changeLineIfNecessary(operator);
            if (operator.type() == TokenType.NOT) {
                byteCodeBuilder.addSimple(Opcode.NOT);
            }
            else {
                Opcode neg = getNeg(expr.executor());
                byteCodeBuilder.addSimple(neg);
            }
        }
        retainExprResult = hadRetain;
        return null;
    }

    @Override
    public Void visitVarRefExpr(Expr.VarRef expr) {
        if (retainExprResult) {
            byteCodeBuilder.changeLineIfNecessary(expr.name());
            getVar(expr.ordinal());
        } else
            ignoredExprResult = true;
        return null;
    }

    @Override
    public Void visitConstructorExpr(Expr.Constructor expr) {
        byteCodeBuilder.changeLineIfNecessary(expr.keyword());
        ScriptedClass target = expr.target().get();
        byteCodeBuilder.addStringInstruction(Opcode.NEW, VarTypeManager.getClassName(target));

        if (expr.signature() != null) {
            if (retainExprResult) {
                byteCodeBuilder.addSimple(Opcode.DUP);
            } else {
                ignoredExprResult = true;
            }
            saveArgs(expr.args());
            byteCodeBuilder.addStringInstruction(Opcode.INVOKE_VIRTUAL, expr.signature());
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
        if (!ignoredExprResult) {
            byteCodeBuilder.addSimple(Opcode.POP);
        }
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "if");
        retainExprResult = true;
        byteCodeBuilder.changeLineIfNecessary(stmt.keyword());
        cache(stmt.condition());
        int instPatch = byteCodeBuilder.addJumpIfFalse();
        retainExprResult = false;
        cache(stmt.thenBranch());
        if (stmt.elifs().length > 0 || stmt.elseBranch() != null) {
            List<Integer> instBranches = new ArrayList<>();
            if (!stmt.branchSeenReturn())
                instBranches.add(byteCodeBuilder.addJump()); //jump over else-ifs & else
            for (int i = 0; i < stmt.elifs().length; i++) {
                byteCodeBuilder.patchJump(instPatch);
                ElifBranch branch = stmt.elifs()[i];
                cache(branch.condition());
                instPatch = byteCodeBuilder.addJumpIfFalse();
                retainExprResult = false;
                cache(branch.body());
                if (!branch.seenReturn()) {
                    instBranches.add(byteCodeBuilder.addJump());
                }
            }
            if (stmt.elseBranch() != null) {
                byteCodeBuilder.patchJump(instPatch);
                retainExprResult = false;
                cache(stmt.elseBranch());
            }
            byteCodeBuilder.addJumpMultiTargetInstruction(instBranches);
        } else {
            byteCodeBuilder.patchJump(instPatch);
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value() != null) {
            retainExprResult = true;
            byteCodeBuilder.changeLineIfNecessary(stmt.keyword());
            cache(stmt.value());
            byteCodeBuilder.addSimple(Opcode.RETURN_ARG);
        } else {
            byteCodeBuilder.addSimple(Opcode.RETURN);
        }
        return null;
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        retainExprResult = true;
        cache(stmt.value());
        byteCodeBuilder.changeLineIfNecessary(stmt.keyword());
        byteCodeBuilder.addSimple(Opcode.THROW);
        return null;
    }

    @Override
    public Void visitVarDeclStmt(Stmt.VarDecl stmt) {
        retainExprResult = true;
        byteCodeBuilder.changeLineIfNecessary(stmt.name());
        cacheOrNull(stmt.initializer()); //adding a value to the stack without removing it automatically adds it as a local variable
        byteCodeBuilder.registerLocal(stmt.localId(), stmt.type(), stmt.name().lexeme());
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        int index = byteCodeBuilder.jumpTarget();
        retainExprResult = true;
        byteCodeBuilder.changeLineIfNecessary(stmt.keyword());
        cache(stmt.condition());
        int skip = byteCodeBuilder.addJumpIfFalse();
        loops.add(new Loop((short) index));
        retainExprResult = false;
        cache(stmt.body());
        byteCodeBuilder.addJump(index);
        loops.pop().patchBreaks();
        byteCodeBuilder.patchJump(skip);
        return null;
    }

    //clears locals off the stack when they move out of scope
    @Override
    public Void visitClearLocalsStmt(Stmt.ClearLocals stmt) {
        int amount = stmt.amount();
        while (amount >= 2) {
            byteCodeBuilder.addSimple(Opcode.POP_2);
            amount -= 2;
        }
        if (amount > 0)
            byteCodeBuilder.addSimple(Opcode.POP);
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        byteCodeBuilder.changeLineIfNecessary(stmt.keyword());
        cache(stmt.init());
        int result = byteCodeBuilder.size();
        retainExprResult = true;
        ignoredExprResult = false;
        cache(stmt.condition());
        int jump1 = byteCodeBuilder.addJumpIfFalse();
        loops.add(new Loop((short) result));
        retainExprResult = false;
        ignoredExprResult = false;
        cache(stmt.body());
        retainExprResult = false;
        ignoredExprResult = false;
        cache(stmt.increment());
        if (!ignoredExprResult)
            byteCodeBuilder.addSimple(Opcode.POP); //pop the result of the increment
        byteCodeBuilder.addJump(result);
        loops.pop().patchBreaks();

        byteCodeBuilder.patchJump(jump1);
        return null;
    }

    @Override
    public Void visitForEachStmt(Stmt.ForEach stmt) {
        retainExprResult = true;
        byteCodeBuilder.changeLineIfNecessary(stmt.name());
        cache(stmt.initializer()); //create array variable
        byteCodeBuilder.registerLocal(stmt.baseVar(), stmt.type().array(), "$array");
        byteCodeBuilder.addSimple(Opcode.I_0);//create iteration variable
        byteCodeBuilder.registerLocal(stmt.baseVar() + 1, VarTypeManager.INTEGER.reference(), "$i");
        int baseVarIndex = stmt.baseVar();


        int curIndex = byteCodeBuilder.jumpTarget(); //link to jump back when loop is completed

        //region condition
        getVar(baseVarIndex); //get array var
        byteCodeBuilder.addSimple(Opcode.ARRAY_LENGTH); //get length of array
        getVar(baseVarIndex + 1); //get iteration var
        byteCodeBuilder.addSimple(Opcode.I_LESSER); //check if iteration var is less than the length of the array
        int result = byteCodeBuilder.addJumpIfFalse(); //create jump out of the loop if check fails
        //endregion
        loops.add(new Loop((short) result)); //push loop

        //region load iteration object
        getVar(baseVarIndex + 1); //load iteration var
        getVar(baseVarIndex); //load array var
        byteCodeBuilder.addSimple(getArrayLoad(stmt.type()));  //create entry var by loading array element
        byteCodeBuilder.registerLocal(stmt.baseVar() + 2, stmt.type(), stmt.name().lexeme());
        //endregion

        retainExprResult = false;
        cache(stmt.body()); //cache loop body

        //region increase iteration var
        byteCodeBuilder.addSimple(Opcode.I_1); //load 1
        getVar(baseVarIndex + 1); //get iteration var
        byteCodeBuilder.addSimple(Opcode.I_ADD); //add 1 to the iteration var
        assignVar(baseVarIndex + 1);
        //endregion
        loops.pop().patchBreaks();
        byteCodeBuilder.addJump(curIndex);
        byteCodeBuilder.patchJump(result);
        return null;
    }

    @Override
    public Void visitDebugTraceStmt(Stmt.DebugTrace stmt) {
        byteCodeBuilder.add(new TraceDebugInstruction(stmt.locals()));
        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        byteCodeBuilder.changeLineIfNecessary(stmt.type());
        Loop loop = loops.peek();
        switch (stmt.type().type()) {
            case BREAK -> loop.addBreak(byteCodeBuilder.addJump());
            case CONTINUE -> byteCodeBuilder.addJump(loop.condition);
        }
        return null;
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        int handlerStart = byteCodeBuilder.size();
        retainExprResult = false;
        cache(stmt.body());
        int handlerEnd = byteCodeBuilder.size();
        List<Integer> jumps = new ArrayList<>();
        jumps.add(byteCodeBuilder.addJump());
        for (Pair<Pair<ClassReference[], Token>, Stmt.Block> aCatch : stmt.catches()) {
            for (ClassReference reference : aCatch.left().left()) {
                byteCodeBuilder.addExceptionHandler(handlerStart, handlerEnd, VarTypeManager.getClassName(reference.get()));
            }
            retainExprResult = false;
            cache(aCatch.right());
            jumps.add(byteCodeBuilder.addJump());
        }
        if (stmt.finale() != null) {
            byteCodeBuilder.addExceptionHandler(handlerStart, handlerEnd, null);
            retainExprResult = false;
            cache(stmt.finale());
        }
        byteCodeBuilder.addJumpMultiTargetInstruction(jumps);

        //TODO add https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.10
        //also read this: https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.3
        return null;
    }

    private void assignVar(int i) {
        switch (i) { //save the iteration var
            case 0 -> byteCodeBuilder.addSimple(Opcode.ASSIGN_0);
            case 1 -> byteCodeBuilder.addSimple(Opcode.ASSIGN_1);
            case 2 -> byteCodeBuilder.addSimple(Opcode.ASSIGN_2);
            default -> {
                byteCodeBuilder.addLocalAccess(Opcode.ASSIGN, i);
            }
        }
    }

    private void getVar(int i) {

        switch (i) {
            case 0 -> byteCodeBuilder.addSimple(Opcode.GET_0);
            case 1 -> byteCodeBuilder.addSimple(Opcode.GET_1);
            case 2 -> byteCodeBuilder.addSimple(Opcode.GET_2);
            default -> byteCodeBuilder.addLocalAccess(Opcode.GET, i);
        }

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
    private void assign(ClassReference retType, Expr value, Token type, Opcode get, Opcode assign, Consumer<Opcode> instSink) {
        boolean hadRetain = retainExprResult;
        retainExprResult = true;
        cache(value);
        byteCodeBuilder.changeLineIfNecessary(type);
        if (type.type() != TokenType.ASSIGN) {
            instSink.accept(get);
            Opcode operation = switch (type.type()) {
                case ADD_ASSIGN -> getAdd(retType);
                case SUB_ASSIGN -> getSub(retType);
                case MUL_ASSIGN -> getMul(retType);
                case DIV_ASSIGN -> getDiv(retType);
                case POW_ASSIGN -> getPow(retType);
                default -> throw new IllegalStateException("no operation for type: " + type.type());
            };
            byteCodeBuilder.addSimple(operation);
        }
        if (hadRetain) {
            byteCodeBuilder.addSimple(Opcode.DUP);
        } else
            ignoredExprResult = true;
        retainExprResult = hadRetain;
        instSink.accept(assign);
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
            byteCodeBuilder.addJumpMultiTargetInstruction(this.breakIndices);
        }
    }
}
