package net.kapitencraft.lang.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.bytecode.exe.Chunk;
import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.compiler.visitor.RetTypeFinder;
import net.kapitencraft.lang.holder.LiteralHolder;
import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.holder.ast.CompileStmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class CacheBuilder implements CompileExpr.Visitor<Void>, CompileStmt.Visitor<Void> {
    public static final int majorVersion = 1, minorVersion = 0;

    private Chunk.Builder builder = new Chunk.Builder();
    private final RetTypeFinder typeFinder;
    private final Stack<Loop> loops = new Stack<>();

    public CacheBuilder() {
        this.typeFinder = null;
    }

    //TODO implement
    public void cache(CompileExpr expr) {
        expr.accept(this);
    }

    private void cacheOrNull(@Nullable CompileExpr expr) {
        if (expr == null) builder.addCode(Opcode.NULL);
        else cache(expr);
    }

    public void cache(CompileStmt stmt) {
        stmt.accept(this);
    }

    public void saveArgs(CompileExpr[] args) {
        for (CompileExpr arg : args) {
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
    public Void visitAssignExpr(CompileExpr.Assign expr) {
        ClassReference reference = typeFinder.findRetType(expr.value());
        assign(reference, expr.value(), expr.type().type(), Opcode.GET, Opcode.ASSIGN, b -> b.addArg(expr.ordinal()));
        return null;
    }

    @Override
    public Void visitSpecialAssignExpr(CompileExpr.SpecialAssign expr) {
        //switch (expr.assignType.type()) {
        //    case GROW ->
        //}
        //builder.addCode(Opcode.GET);
        //builder.addArg(expr.ordinal);
        //builder.addCode(getAdd(expr.));
        //builder.addCode(Opcode.ASSIGN);
        //builder.addArg(expr.ordinal);
        return null;
    }

    @Override
    public Void visitBinaryExpr(CompileExpr.Binary expr) {
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
    public Void visitWhenExpr(CompileExpr.When expr) {
        cache(expr.condition());
        this.builder.jumpElse(() -> cache(expr.ifTrue()), () -> cache(expr.ifFalse()));
        return null;
    }

    @Override
    public Void visitInstCallExpr(CompileExpr.InstCall expr) {
        cache(expr.callee());
        saveArgs(expr.args());
        builder.addCode(Opcode.INVOKE);
        builder.injectString(expr.name().lexeme());
        return null;
    }

    @Override
    public Void visitStaticCallExpr(CompileExpr.StaticCall expr) {
        saveArgs(expr.args());
        builder.addCode(Opcode.INVOKE);
        builder.injectString(expr.name().lexeme());
        return null;
    }

    @Override
    public Void visitGetExpr(CompileExpr.Get expr) {
        builder.addCode(Opcode.GET_FIELD);
        builder.injectString(expr.name().lexeme());
        return null;
    }

    @Override
    public Void visitStaticGetExpr(CompileExpr.StaticGet expr) {
        builder.addCode(Opcode.GET_STATIC);
        builder.injectString(expr.name().lexeme());
        return null;
    }

    @Override
    public Void visitArrayGetExpr(CompileExpr.ArrayGet expr) {
        cache(expr.object());
        cache(expr.index());
        ClassReference reference = typeFinder.findRetType(expr.object());
        builder.addCode(getArrayLoad(reference));
        return null;
    }

    @Override
    public Void visitSetExpr(CompileExpr.Set expr) {

        ClassReference reference = typeFinder.findRetType(expr.object());
        assign(reference, expr.value(), expr.assignType().type(), Opcode.GET_FIELD, Opcode.PUT_FIELD, b -> b.injectString(expr.name().lexeme()));
        return null;
    }

    @Override
    public Void visitStaticSetExpr(CompileExpr.StaticSet expr) {
        //TODO store return type in set expressions
        //assign();

        return null;
    }

    @Override
    public Void visitArraySetExpr(CompileExpr.ArraySet expr) {
        ClassReference reference = typeFinder.findRetType(expr.value());
        cache(expr.value());
        if (expr.assignType().type() != TokenType.ASSIGN) {

            builder.addCode(getArrayLoad(reference));
            switch (expr.assignType().type()) {
                case ADD_ASSIGN -> builder.addCode(getAdd(reference));
                case SUB_ASSIGN -> builder.addCode(getSub(reference));
                case MUL_ASSIGN -> builder.addCode(getMul(reference));
                case DIV_ASSIGN -> builder.addCode(getDiv(reference));
                case POW_ASSIGN -> builder.addCode(getPow(reference));
            }
        } else {
            cache(expr.index());
            builder.addCode(getArrayStore(reference));
        }
        return null;
    }

    @Override
    public Void visitSpecialSetExpr(CompileExpr.SpecialSet expr) {
        return null;
    }

    @Override
    public Void visitStaticSpecialExpr(CompileExpr.StaticSpecial expr) {
        return null;
    }

    @Override
    public Void visitArraySpecialExpr(CompileExpr.ArraySpecial expr) {
        //TODO make work
        ClassReference reference = typeFinder.findRetType(expr.object());
        builder.addCode(expr.assignType().type() == TokenType.GROW ?
                getPlusOne(reference) :
                getMinusOne(reference)
        );
        cache(expr.index());
        cache(expr.object());
        builder.addCode(getArrayLoad(reference));
        builder.addCode(getAdd(reference));
        builder.addCode(getArrayStore(reference));
        return null;
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
    public Void visitSliceExpr(CompileExpr.Slice expr) {
        cache(expr.object());
        cacheOrNull(expr.start());
        cacheOrNull(expr.end());
        cacheOrNull(expr.interval());
        builder.addCode(Opcode.SLICE);
        return null;
    }

    @Override
    public Void visitSwitchExpr(CompileExpr.Switch expr) {
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
    public Void visitCastCheckExpr(CompileExpr.CastCheck expr) {
        return null;
    }

    @Override
    public Void visitGroupingExpr(CompileExpr.Grouping expr) {
        cache(expr.expression());
        return null;
    }

    @Override
    public Void visitLiteralExpr(CompileExpr.Literal expr) {
        LiteralHolder literal = expr.literal().literal();
        ScriptedClass scriptedClass = literal.type();
        Object value = literal.value();
        if (scriptedClass == VarTypeManager.DOUBLE) builder.addDoubleConstant((double) value);
        else if (scriptedClass == VarTypeManager.INTEGER) builder.addIntConstant((int) value);
        else if (scriptedClass == VarTypeManager.STRING) builder.addStringConstant((String) value);
        return null;
    }

    @Override
    public Void visitLogicalExpr(CompileExpr.Logical expr) {
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
    public Void visitUnaryExpr(CompileExpr.Unary expr) {
        cache(expr.right());
        if (expr.operator().type() == TokenType.NOT) builder.addCode(Opcode.NOT);
        else builder.addCode(getNeg(typeFinder.findRetType(expr.right())));
        return null;
    }

    @Override
    public Void visitVarRefExpr(CompileExpr.VarRef expr) {
        builder.addCode(Opcode.GET);
        builder.addArg(expr.ordinal());
        return null;
    }

    @Override
    public Void visitConstructorExpr(CompileExpr.Constructor expr) {
        return null;
    }

    @Override
    public Void visitBlockStmt(CompileStmt.Block stmt) {
        CompileStmt[] statements = stmt.statements();
        for (int i = statements.length - 1; i >= 0; i--) {
            this.cache(statements[i]);
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(CompileStmt.Expression stmt) {
        cache(stmt.expression());
        return null;
    }

    @Override
    public Void visitIfStmt(CompileStmt.If stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "if");
        cache(stmt.condition());
        int jumpPatch = builder.addJumpIfFalse();
        cache(stmt.thenBranch());
        if (stmt.elifs().length > 0 || stmt.elseBranch() != null) {
            int[] branches = new int[stmt.elifs().length + 1];
            branches[0] = builder.addJump(); //jump from branch past the IF
            for (int i = 0; i < branches.length; i++) {
                builder.patchJumpCurrent(jumpPatch);
                Pair<CompileExpr, CompileStmt> pair = stmt.elifs()[i];
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
    public Void visitReturnStmt(CompileStmt.Return stmt) {
        cache(stmt.value());
        builder.addCode(Opcode.RETURN);
        return null;
    }

    @Override
    public Void visitThrowStmt(CompileStmt.Throw stmt) {
        cache(stmt.value());
        builder.addCode(Opcode.THROW);
        return null;
    }

    @Override
    public Void visitVarDeclStmt(CompileStmt.VarDecl stmt) {
        cacheOrNull(stmt.initializer());
        return null;
    }

    @Override
    public Void visitWhileStmt(CompileStmt.While stmt) {
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
    public Void visitForStmt(CompileStmt.For stmt) {
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
    public Void visitForEachStmt(CompileStmt.ForEach stmt) {
        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(CompileStmt.LoopInterruption stmt) {
        Loop loop = loops.peek();
        switch (stmt.type().type()) {
            case BREAK -> loop.addBreak(builder.addJump());
            case CONTINUE -> builder.patchJump(builder.addJump(), loop.condition);
        }
        return null;
    }

    @Override
    public Void visitTryStmt(CompileStmt.Try stmt) {
        //JsonObject object = new JsonObject();
        //object.addProperty("TYPE", "try");
        //object.add("body", cache(stmt.body));
        //JsonArray array = new JsonArray();
        //for (Pair<Pair<ClassReference[], Token>, CompileStmt.Block> pair : stmt.catches) {
        //    JsonObject pairDat = new JsonObject();
        //    Pair<ClassReference[], Token> pair1 = pair.left();
        //    JsonObject pair1Dat = new JsonObject();
        //    JsonArray classes = new JsonArray();
        //    Arrays.stream(pair1.left()).map(ClassReference::absoluteName).forEach(classes::add);
        //    pair1Dat.add("classes", classes);
        //    pair1Dat.addProperty("name", pair1.right().lexeme());
        //    pairDat.add("initData", pair1Dat);
        //    pairDat.add("executor", cache(pair.right()));
        //    array.add(pairDat);
        //}
        //object.add("catches", array);
        //if (stmt.finale != null) object.add("finale", cache(stmt.finale));
        return null;
    }

    public Chunk.Builder setup() {
        this.builder.clear();
        return this.builder;
    }

    private Opcode getArrayLoad(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.IA_LOAD;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.DA_LOAD;
        throw new IllegalStateException("could not create 'a_load' for: " + reference);
    }

    private Opcode getArrayStore(ClassReference reference) {
        if (reference.is(VarTypeManager.INTEGER)) return Opcode.IA_STORE;
        if (reference.is(VarTypeManager.DOUBLE)) return Opcode.DA_STORE;
        throw new IllegalStateException("could not create 'a_store' for: " + reference);
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

    private void assign(ClassReference retType, CompileExpr value, TokenType type, Opcode get, Opcode assign, Consumer<Chunk.Builder> meta) {
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

        public short conditionIndex() {
            return condition;
        }

        public void addBreak(int patchIndex) {
            this.breakIndices.add(patchIndex);
        }

        public void patchBreaks() {
            this.breakIndices.forEach(builder::patchJumpCurrent);
        }
    }
}
