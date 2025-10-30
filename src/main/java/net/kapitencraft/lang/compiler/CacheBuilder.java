package net.kapitencraft.lang.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.bytecode.exe.Chunk;
import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.bytecode.exe.VirtualMachine;
import net.kapitencraft.lang.holder.LiteralHolder;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class CacheBuilder implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    public static final int majorVersion = 1, minorVersion = 0;

    //TODO add line number and local variable table attributes
    private final Chunk.Builder builder = new Chunk.Builder();
    private final Stack<Loop> loops = new Stack<>();

    public CacheBuilder() {
    }

    public void cache(Expr expr) {
        expr.accept(this);
    }

    private void cacheOrNull(@Nullable Expr expr) {
        if (expr == null) builder.addCode(Opcode.NULL);
        else cache(expr);
    }

    public void cache(Stmt stmt) {
        stmt.accept(this);
    }

    public void saveArgs(Expr[] args) {
        for (Expr arg : args) {
            this.cache(arg);
        }
    }

    public JsonObject cacheClass(CacheableClass loxClass) {
        return loxClass.save(this); //TODO convert to entirely bytecode later
    }

    public JsonArray cacheAnnotations(CompileAnnotationClassInstance[] annotations) {
        JsonArray array = new JsonArray();
        for (CompileAnnotationClassInstance instance : annotations) {
            CompileAnnotationClassInstance retention;
            //if (retention = instance.getType().getAnnotation(VarTypeManager.RETENTION) != null) {
            //    if (retention.getProperty("value"))
            //    //TODO create annotation processor and implement retention
            //}
            JsonObject data = new JsonObject();
            data.addProperty("type", instance.getType().absoluteName());
            JsonObject properties = new JsonObject();
            //instance.properties.forEach((string, expr) -> properties.add(string, cache(expr)));
            data.add("properties", properties);
            array.add(data);
        }
        return array;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        Opcode get = Opcode.GET;
        Opcode assign = Opcode.ASSIGN;
        switch (expr.ordinal()) {
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
        assign(expr.executor(), expr.value(), expr.type().type(), get, assign, b -> {
            if (expr.ordinal() > 2) b.addArg(expr.ordinal());
        });
        return null;
    }

    @Override
    public Void visitSpecialAssignExpr(Expr.SpecialAssign expr) {

        specialAssign(expr.executor(), expr.assignType(), Opcode.GET, Opcode.ASSIGN, b -> b.addArg(expr.ordinal()));
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        cache(expr.right());
        cache(expr.left());
        final ClassReference executor = expr.executor();
        switch (expr.operator().type()) {
            case EQUAL -> builder.addCode(Opcode.EQUAL);
            case NEQUAL -> builder.addCode(Opcode.NEQUAL);
            case LEQUAL -> builder.addCode(getLequal(executor));
            case GEQUAL -> builder.addCode(getGequal(executor));
            case LESSER -> builder.addCode(getLesser(executor));
            case GREATER -> builder.addCode(getGreater(executor));
            case SUB -> builder.addCode(getSub(executor));
            case ADD -> builder.addCode(getAdd(executor));
            case MUL -> builder.addCode(getMul(executor));
            case DIV -> builder.addCode(getDiv(executor));
            case POW -> builder.addCode(getPow(executor));
        }
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
        return null;
    }

    @Override
    public Void visitInstCallExpr(Expr.InstCall expr) {
        cache(expr.callee());
        saveArgs(expr.args());
        builder.invokeVirtual(expr.id());
        return null;
    }

    @Override
    public Void visitStaticCallExpr(Expr.StaticCall expr) {
        saveArgs(expr.args());
        builder.invokeStatic(expr.id());
        return null;
    }

    @Override
    public Void visitSuperCallExpr(Expr.SuperCall expr) {
        cache(expr.callee());
        saveArgs(expr.args());
        builder.invokeStatic(expr.id());

        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        builder.addCode(Opcode.GET_FIELD);
        builder.injectString(expr.name().lexeme());
        return null;
    }

    @Override
    public Void visitStaticGetExpr(Expr.StaticGet expr) {
        builder.addCode(Opcode.GET_STATIC);
        builder.injectString(VarTypeManager.getClassName(expr.target().get()));
        builder.injectString(expr.name().lexeme());
        return null;
    }

    @Override
    public Void visitArrayGetExpr(Expr.ArrayGet expr) {
        cache(expr.index());
        cache(expr.object());
        builder.addCode(getArrayLoad(expr.type()));
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {

        ClassReference retType = expr.executor();
        TokenType type = expr.assignType().type();
        cache(expr.value());
        cache(expr.object());
        if (type != TokenType.ASSIGN) {
            builder.addCode(Opcode.DUP_X1); //duplicate object down so the get field
            builder.addCode(Opcode.GET_FIELD);
            builder.injectString(expr.name().lexeme());
            switch (type) {
                case ADD_ASSIGN -> builder.addCode(getAdd(retType));
                case SUB_ASSIGN -> builder.addCode(getSub(retType));
                case MUL_ASSIGN -> builder.addCode(getMul(retType));
                case DIV_ASSIGN -> builder.addCode(getDiv(retType));
                case POW_ASSIGN -> builder.addCode(getPow(retType));
            }
        }
        builder.addCode(Opcode.DUP);
        builder.addCode(Opcode.PUT_FIELD);
        builder.injectString(expr.name().lexeme());
        return null;
    }

    @Override
    public Void visitStaticSetExpr(Expr.StaticSet expr) {

        cache(expr.value());
        TokenType type = expr.assignType().type();
        String className = VarTypeManager.getClassName(expr.target().get());
        String fieldName = expr.name().lexeme();
        ClassReference retType = expr.executor();

        if (type != TokenType.ASSIGN) {
            builder.addCode(Opcode.GET_STATIC);
            builder.injectString(className);
            builder.injectString(fieldName);
            switch (type) {
                case ADD_ASSIGN -> builder.addCode(getAdd(retType));
                case SUB_ASSIGN -> builder.addCode(getSub(retType));
                case MUL_ASSIGN -> builder.addCode(getMul(retType));
                case DIV_ASSIGN -> builder.addCode(getDiv(retType));
                case POW_ASSIGN -> builder.addCode(getPow(retType));
            }
        }
        builder.addCode(Opcode.DUP);
        builder.addCode(Opcode.PUT_STATIC);
        builder.injectString(className);
        builder.injectString(fieldName);

        return null;
    }

    @Override
    public Void visitArraySetExpr(Expr.ArraySet expr) {
        ClassReference retType = expr.executor();
        TokenType type = expr.assignType().type();
        cache(expr.value());
        cache(expr.index());
        cache(expr.object());
        if (type != TokenType.ASSIGN) {
            builder.addCode(Opcode.DUP2_X1);
            builder.addCode(getArrayLoad(retType));
            switch (type) {
                case ADD_ASSIGN -> builder.addCode(getAdd(retType));
                case SUB_ASSIGN -> builder.addCode(getSub(retType));
                case MUL_ASSIGN -> builder.addCode(getMul(retType));
                case DIV_ASSIGN -> builder.addCode(getDiv(retType));
                case POW_ASSIGN -> builder.addCode(getPow(retType));
            }
        }
        builder.addCode(Opcode.DUP); //duplicate to keep the value on the stack as the ARRAY_SET does not actually keep anything on the stack
        builder.addCode(getArrayStore(retType));
        return null;
    }

    @Override
    public Void visitSpecialSetExpr(Expr.SpecialSet expr) {

        cache(expr.callee());
        specialAssign(expr.retType(), expr.assignType(), Opcode.GET_FIELD, Opcode.PUT_FIELD, b -> b.injectString(expr.name().lexeme()));
        return null;
    }

    @Override
    public Void visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        String id = VarTypeManager.getClassName(expr.target().get());

        ClassReference reference = expr.executor();
        builder.addCode(expr.assignType().type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference)
        );

        specialAssign(expr.executor(), expr.assignType(), Opcode.GET_STATIC, Opcode.PUT_STATIC, b -> b.injectString(id));
        return null;
    }

    @Override
    public Void visitArraySpecialExpr(Expr.ArraySpecial expr) {
        ClassReference reference = expr.executor();
        builder.addCode(expr.assignType().type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference)
        );
        builder.addCode(getAdd(reference));
        cache(expr.index());
        cache(expr.object());
        builder.addCode(Opcode.DUP2_X1);
        return null;
    }

    private void specialAssign(ClassReference reference, Token token, Opcode get, Opcode set, Consumer<Chunk.Builder> meta) {
        builder.addCode(get);
        meta.accept(builder); //TODO fix multiple invokes
        builder.addCode(token.type() == TokenType.GROW ?
                getPlusOne(reference) : getMinusOne(reference)
        );
        builder.addCode(getAdd(reference));
        builder.addCode(Opcode.DUP); //duplicate value to emit it onto the object stack
        builder.addCode(set);
        meta.accept(builder);
    }

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

    @Override
    public Void visitSliceExpr(Expr.Slice expr) {
        cache(expr.object());
        cacheOrNull(expr.start());
        cacheOrNull(expr.end());
        cacheOrNull(expr.interval());
        builder.addCode(Opcode.SLICE);
        return null;
    }

    @Override
    public Void visitSwitchExpr(Expr.Switch expr) {
        //JsonObject object = new JsonObject(); TODO fix
        //cache(expr.provider);
        //this.builder.addCode(Opcode.SWITCH);
//
        //object.addProperty("TYPE", "switch");
        //object.add("defaulted", cache(expr.defaulted));
        //object.add("keyword", expr.keyword.toJson());
        //object.add("elements", Util.writeMap(
        //        expr.params,
        //        o -> o instanceof Number n ? new JsonPrimitive(n) : o instanceof Boolean b ? new JsonPrimitive(b) : o instanceof String s ? new JsonPrimitive(s) : new JsonPrimitive((char) o),
        //        this::cache)
        //);
        //return object;
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
        LiteralHolder literal = expr.literal().literal();
        ScriptedClass scriptedClass = literal.type();
        Object value = literal.value();
        if (scriptedClass == VarTypeManager.DOUBLE) {
            double v = (double) value;
            if (v == 1d)
                builder.addCode(Opcode.D_1);
            else if (v == -1d) {
                builder.addCode(Opcode.D_M1);
            } else
                builder.addDoubleConstant(v);
        } else if (scriptedClass == VarTypeManager.INTEGER) {
            int v = (int) value;
            builder.addInt(v);
        } else if (VarTypeManager.STRING.is(scriptedClass))
            builder.addStringConstant((String) value);
        else if (VarTypeManager.FLOAT.is(scriptedClass)) {
            float v = (float) value;
            if (v == 1f)
                builder.addCode(Opcode.F_1);
            else if (v == -1f)
                builder.addCode(Opcode.F_M1);
            else
                builder.addFloatConstant(v);
        }
        return null;
    }

    @Override
    public Void visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
        cache(expr.size());
        builder.addCode(getArrayNew(expr.compoundType()));
        builder.injectString(VarTypeManager.getClassName(expr.compoundType().get()));
        Expr[] objects = expr.obj();
        Opcode store = getArrayStore(expr.compoundType());
        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                builder.addCode(Opcode.DUP);
                builder.addInt(i);
                cache(objects[i]);
                builder.addCode(store);
            }
        }
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        cache(expr.right());
        cache(expr.left());
        builder.addCode(switch (expr.operator().type()) {
            case OR -> Opcode.OR;
            case XOR -> Opcode.XOR;
            case AND -> Opcode.AND;
            default -> throw new IllegalArgumentException("unknown logical type: " + expr.operator());
        });
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        cache(expr.right());
        if (expr.operator().type() == TokenType.NOT) builder.addCode(Opcode.NOT);
        else builder.addCode(getNeg(expr.executor()));
        return null;
    }

    @Override
    public Void visitVarRefExpr(Expr.VarRef expr) {
        getVar(expr.ordinal());
        return null;
    }

    @Override
    public Void visitConstructorExpr(Expr.Constructor expr) {
        builder.addCode(Opcode.NEW);
        ScriptedClass target = expr.target().get();
        builder.injectString(VarTypeManager.getClassName(target));

        if (expr.signature() != null) {
            builder.addCode(Opcode.DUP); //duplicate object to enable invoke
            saveArgs(expr.params());
            //TODO
            builder.invokeVirtual(expr.signature());
            builder.addCode(Opcode.POP); //pop method invoke result
        }

        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        Stmt[] statements = stmt.statements();
        for (int i = statements.length - 1; i >= 0; i--) {
            this.cache(statements[i]);
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        cache(stmt.expression());
        builder.addCode(Opcode.POP); //TODO check for array assignment and re-store
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "if");
        cache(stmt.condition());
        int jumpPatch = builder.addJumpIfFalse();
        cache(stmt.thenBranch());
        if (stmt.elifs().length > 0 || stmt.elseBranch() != null) {
            int[] branches = new int[stmt.elifs().length + 1];
            branches[0] = builder.addJump(); //jump from branch past the IF
            for (int i = 0; i < stmt.elifs().length; i++) {
                builder.patchJumpCurrent(jumpPatch);
                Pair<Expr, Stmt> pair = stmt.elifs()[i];
                cache(pair.left());
                jumpPatch = builder.addJumpIfFalse();
                cache(pair.right());
                branches[i + 1] = builder.addJump();
            }
            if (stmt.elseBranch() != null) {
                builder.patchJumpCurrent(jumpPatch);
                cache(stmt.elseBranch());
            }
            for (int branch : branches) {
                builder.patchJumpCurrent(branch);
            }
        } else builder.patchJumpCurrent(jumpPatch);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        cache(stmt.value());
        builder.addCode(Opcode.RETURN);
        return null;
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        cache(stmt.value());
        builder.addCode(Opcode.THROW);
        return null;
    }

    @Override
    public Void visitVarDeclStmt(Stmt.VarDecl stmt) {
        cacheOrNull(stmt.initializer()); //adding a value to the stack without removing it automatically adds it as a local variable
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        int index = builder.currentCodeIndex();
        cache(stmt.condition());
        int skip = builder.addJumpIfFalse();
        loops.add(new Loop((short) index));
        cache(stmt.body());
        int returnIndex = builder.addJump();
        loops.pop().patchBreaks();
        builder.patchJumpCurrent(skip);
        builder.patchJump(returnIndex, (short) index);
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        cache(stmt.init());
        int result = builder.addJumpIfFalse();
        cache(stmt.condition());
        int jump1 = builder.addJumpIfFalse();
        loops.add(new Loop((short) result));
        cache(stmt.body());
        cache(stmt.increment());
        int returnIndex = builder.addJump();
        loops.pop().patchBreaks();
        builder.patchJumpCurrent(jump1);
        builder.patchJump(returnIndex, (short) result);
        return null;
    }

    @Override
    public Void visitForEachStmt(Stmt.ForEach stmt) {
        builder.addCode(Opcode.I_0); //create iteration variable
        cache(stmt.initializer()); //create array variable
        int baseVarIndex = stmt.baseVar();

        int curIndex = builder.currentCodeIndex(); //link to jump back when loop is completed
        getVar(baseVarIndex + 1); //get array var
        builder.addCode(Opcode.ARRAY_LENGTH); //get length of array
        getVar(baseVarIndex); //get iteration var
        builder.addCode(Opcode.I_LESSER); //check if iteration var is less than the length of the array
        int result = builder.addJumpIfFalse(); //create jump out of the loop if check fails
        loops.add(new Loop((short) result)); //push loop

        getVar(baseVarIndex); //load iteration var
        getVar(baseVarIndex + 1); //load array var
        builder.addCode(getArrayLoad(stmt.type()));  //create entry var by loading array element
        assignVar(baseVarIndex + 2); //store value to entry var
        builder.addCode(Opcode.POP); //pop assign TODO perhaps inline it with further code analysis

        cache(stmt.body()); //cache loop body

        builder.addCode(Opcode.I_1); //load 1
        getVar(baseVarIndex); //get iteration var
        builder.addCode(Opcode.I_ADD); //add 1 to the iteration var
        assignVar(baseVarIndex);
        int returnIndex = builder.addJump();
        loops.pop().patchBreaks();
        builder.patchJumpCurrent(result);
        builder.patchJump(returnIndex, (short) curIndex);
        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
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
        cache(stmt.body());
        int handlerEnd = builder.currentCodeIndex();
        List<Integer> jumps = new ArrayList<>();
        jumps.add(builder.addJump());
        for (Pair<Pair<ClassReference[], Token>, Stmt.Block> aCatch : stmt.catches()) {
            for (ClassReference reference : aCatch.left().left()) {
                builder.addExceptionHandler(handlerStart, handlerEnd, builder.currentCodeIndex(), builder.injectStringNoArg(VarTypeManager.getClassName(reference.get())));
            }
            cache(aCatch.right());
            jumps.add(builder.addJump());
        }
        if (stmt.finale() != null) {
            builder.addExceptionHandler(handlerStart, handlerEnd, builder.currentCodeIndex(), 0);
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
            case 0 -> builder.addCode(Opcode.GET_0);
            case 1 -> builder.addCode(Opcode.GET_1);
            case 2 -> builder.addCode(Opcode.GET_2);
            default -> {
                builder.addCode(Opcode.GET);
                builder.addArg(i);
            }
        }

    }


    public Chunk.Builder setup() {
        this.builder.clear();
        return this.builder;
    }

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

    private void assign(ClassReference retType, Expr value, TokenType type, Opcode get, Opcode assign, Consumer<Chunk.Builder> meta) {
        if (type != TokenType.ASSIGN) {
            builder.addCode(get);
            meta.accept(builder);
            cache(value);
            switch (type) {
                case ADD_ASSIGN -> builder.addCode(getAdd(retType));
                case SUB_ASSIGN -> builder.addCode(getSub(retType));
                case MUL_ASSIGN -> builder.addCode(getMul(retType));
                case DIV_ASSIGN -> builder.addCode(getDiv(retType));
                case POW_ASSIGN -> builder.addCode(getPow(retType));
            }
        } else {
            cache(value);
        }
        builder.addCode(assign);
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
