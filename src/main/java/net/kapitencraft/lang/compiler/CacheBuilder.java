package net.kapitencraft.lang.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kapitencraft.lang.bytecode.exe.Chunk;
import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.holder.LiteralHolder;
import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.holder.ast.CompileStmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.lang.tool.Util;

import java.util.Arrays;

public class CacheBuilder implements CompileExpr.Visitor<Void>, CompileStmt.Visitor<Void> {
    private Chunk.Builder builder;

    //TODO implement
    public void cache(CompileExpr expr) {
        expr.accept(this);
    }

    public void cache(CompileStmt stmt) {
        stmt.accept(this);
    }

    public JsonArray saveArgs(CompileExpr[] args) {
        JsonArray array = new JsonArray();
        Arrays.stream(args).map(this::cache).forEach(array::add);
        return array;
    }

    public JsonObject cacheClass(CacheableClass loxClass) {
        return loxClass.save(this);
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
            instance.properties.forEach((string, expr) -> properties.add(string, cache(expr)));
            data.add("properties", properties);
            array.add(data);
        }
        return array;
    }

    @Override
    public Void visitAssignExpr(CompileExpr.Assign expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "assign");
        object.add("name", expr.name.toJson());
        object.add("value", cache(expr.value));
        object.addProperty("type", expr.type.type().id());
        object.addProperty("line", expr.type.line());
        object.addProperty("executor", expr.executor.get().absoluteName());
        object.addProperty("operand", expr.operand.name());
        return object;
    }

    @Override
    public Void visitSpecialAssignExpr(CompileExpr.SpecialAssign expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "specialAssign");
        object.add("name", expr.name.toJson());
        object.addProperty("assignType", expr.assignType.type().id());
        return object;
    }

    @Override
    public Void visitBinaryExpr(CompileExpr.Binary expr) {
        JsonObject object = new JsonObject();
        cache(expr.right);
        cache(expr.left);
        return null;
    }

    @Override
    public Void visitWhenExpr(CompileExpr.When expr) {
        cache(expr.condition);
        this.builder.jumpElse(() -> cache(expr.ifTrue), () -> cache(expr.ifFalse));
        return null;
    }

    @Override
    public JsonElement visitInstCallExpr(CompileExpr.InstCall expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "instCall");
        object.add("callee", cache(expr.callee));
        object.add("name", expr.name.toJson());
        object.addProperty("ordinal", expr.methodOrdinal);
        object.add("args", saveArgs(expr.args));
        return null;
    }

    @Override
    public JsonElement visitStaticCallExpr(CompileExpr.StaticCall expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "staticCall");
        object.addProperty("target", expr.target.absoluteName());
        object.add("name", expr.name.toJson());
        object.addProperty("ordinal", expr.methodOrdinal);
        object.add("args", saveArgs(expr.args));
        return object;
    }

    @Override
    public JsonElement visitGetExpr(CompileExpr.Get expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "get");
        object.add("callee", cache(expr.object));
        object.addProperty("name", expr.name.lexeme());
        return object;
    }

    @Override
    public JsonElement visitStaticGetExpr(CompileExpr.StaticGet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "staticGet");
        object.addProperty("target", expr.target.absoluteName());
        object.addProperty("name", expr.name.lexeme());
        return object;
    }

    @Override
    public JsonElement visitArrayGetExpr(CompileExpr.ArrayGet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "arrayGet");
        object.add("object", cache(expr.object));
        object.add("index", cache(expr.index));
        return object;
    }

    @Override
    public JsonElement visitSetExpr(CompileExpr.Set expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "set");
        object.add("callee", cache(expr.object));
        object.addProperty("name", expr.name.lexeme());
        object.add("value", cache(expr.value));
        object.addProperty("assignType", expr.assignType.type().id());
        object.addProperty("line", expr.assignType.line());
        object.addProperty("executor", expr.executor.absoluteName());
        object.addProperty("operand", expr.operand.name());
        return object;
    }

    @Override
    public JsonElement visitStaticSetExpr(CompileExpr.StaticSet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "staticSet");
        object.addProperty("target", expr.target.absoluteName());
        object.addProperty("name", expr.name.lexeme());
        object.add("value", cache(expr.value));
        object.addProperty("assignType", expr.assignType.type().id());
        object.addProperty("line", expr.assignType.line());
        object.addProperty("executor", expr.executor.absoluteName());
        object.addProperty("operand", expr.operand.name());
        return object;
    }

    @Override
    public JsonElement visitArraySetExpr(CompileExpr.ArraySet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "arraySet");
        object.add("object", cache(expr.object));
        object.add("index", cache(expr.index));
        object.add("value", cache(expr.value));
        object.addProperty("assign", expr.assignType.type().id());
        object.addProperty("line", expr.assignType.line());
        object.addProperty("executor", expr.executor.absoluteName());
        object.addProperty("operand", expr.operand.name());
        return object;
    }

    @Override
    public JsonElement visitSpecialSetExpr(CompileExpr.SpecialSet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "specialSet");
        object.add("callee", cache(expr.callee));
        object.addProperty("name", expr.name.lexeme());
        object.addProperty("assignType", expr.assignType.type().id());
        return object;
    }

    @Override
    public JsonElement visitStaticSpecialExpr(CompileExpr.StaticSpecial expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "specialStaticSet");
        object.addProperty("target", expr.target.absoluteName());
        object.add("name", expr.name.toJson());
        object.add("assignType", expr.assignType.toJson());
        return object;
    }

    @Override
    public Void visitArraySpecialExpr(CompileExpr.ArraySpecial expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "specialArraySet");
        object.add("object", cache(expr.object));
        object.add("index", cache(expr.index));
        object.addProperty("assign", expr.assignType.type().id());
        return null;
    }

    @Override
    public JsonElement visitSliceExpr(CompileExpr.Slice expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "slice");
        object.add("object", cache(expr.object));
        if (expr.start != null) object.add("start", cache(expr.start));
        if (expr.end != null) object.add("end", cache(expr.end));
        if (expr.interval != null) object.add("interval", cache(expr.interval));
        return object;
    }

    @Override
    public Void visitSwitchExpr(CompileExpr.Switch expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "switch");
        object.add("provider", cache(expr.provider));
        object.add("defaulted", cache(expr.defaulted));
        object.add("keyword", expr.keyword.toJson());
        object.add("elements", Util.writeMap(
                expr.params,
                o -> o instanceof Number n ? new JsonPrimitive(n) : o instanceof Boolean b ? new JsonPrimitive(b) : o instanceof String s ? new JsonPrimitive(s) : new JsonPrimitive((char) o),
                this::cache)
        );
        return object;
    }

    @Override
    public JsonElement visitCastCheckExpr(CompileExpr.CastCheck expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "castCheck");
        object.add("object", cache(expr.object));
        object.addProperty("target", expr.targetType.absoluteName());
        if (expr.patternVarName != null) object.addProperty("patternVarName", expr.patternVarName.lexeme());
        return object;
    }

    @Override
    public Void visitGroupingExpr(CompileExpr.Grouping expr) {
        cache(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(CompileExpr.Literal expr) {
        LiteralHolder literal = expr.literal.literal();
        ScriptedClass scriptedClass = literal.type();
        Object value = literal.value();
        if (scriptedClass == VarTypeManager.DOUBLE) builder.addDoubleConstant((double) value);
        else if (scriptedClass == VarTypeManager.INTEGER) builder.addIntConstant((int) value);
        else if (scriptedClass == VarTypeManager.STRING) builder.addStringConstant((String) value);
        return null;
    }

    @Override
    public Void visitLogicalExpr(CompileExpr.Logical expr) {
        cache(expr.right);
        cache(expr.left);
        builder.addCode(switch (expr.operator.type()) {
            case OR -> Opcode.OR;
            case XOR -> Opcode.XOR;
            case AND -> Opcode.AND;
            default -> throw new IllegalArgumentException("unknown logical type: " + expr.operator);
        });
        return null;
    }

    @Override
    public Void visitUnaryExpr(CompileExpr.Unary expr) {
        cache(expr.right);
        if (expr.operator.type() == TokenType.NOT) builder.addCode(Opcode.NOT);
        else builder.addCode(Opcode.);
        return null;
    }

    @Override
    public Void visitVarRefExpr(CompileExpr.VarRef expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "varRef");
        object.add("name", expr.name.toJson());
        return object;
    }

    @Override
    public JsonElement visitConstructorExpr(CompileExpr.Constructor expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "constructors");
        object.addProperty("target", expr.target.absoluteName());
        object.add("args", saveArgs(expr.params));
        object.addProperty("line", expr.keyword.line());
        object.addProperty("ordinal", expr.ordinal);
        return object;
    }

    @Override
    public Void visitBlockStmt(CompileStmt.Block stmt) {
        for (int i = stmt.statements.length - 1; i >= 0; i--) {
            this.cache(stmt.statements[i]);
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(CompileStmt.Expression stmt) {
        cache(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(CompileStmt.If stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "if");
        cache(stmt.condition);
        int jumpPatch = builder.addJumpIfFalse();
        cache(stmt.thenBranch);
        if (stmt.elifs.length > 0 || stmt.elseBranch != null) {
            int[] branches = new int[stmt.elifs.length + 1];
            branches[0] = builder.addJump(); //jump from branch past the IF
            for (int i = 0; i < branches.length; i++) {
                builder.patchJumpCurrent(jumpPatch);
                Pair<CompileExpr, CompileStmt> pair = stmt.elifs[i];
                cache(pair.left());
                jumpPatch = builder.addJumpIfFalse();
                cache(pair.right());
                branches[i + 1] = builder.addJump();
            }
            if (stmt.elseBranch != null) {
                builder.patchJumpCurrent(jumpPatch);
                cache(stmt.elseBranch);
            }
            for (int branch : branches) {
                builder.patchJumpCurrent(branch);
            }
        } else builder.patchJumpCurrent(jumpPatch);
        return null;
    }

    @Override
    public Void visitReturnStmt(CompileStmt.Return stmt) {
        cache(stmt.value);
        builder.addCode(Opcode.RETURN);
        return null;
    }

    @Override
    public Void visitThrowStmt(CompileStmt.Throw stmt) {
        cache(stmt.value);
        builder.addCode(Opcode.THROW);
        return null;
    }

    @Override
    public JsonElement visitVarDeclStmt(CompileStmt.VarDecl stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "varDecl");
        object.addProperty("name", stmt.name.lexeme());
        object.addProperty("targetType", stmt.type.absoluteName());
        if (stmt.initializer != null) object.add("initializer", cache(stmt.initializer));
        object.addProperty("isFinal", stmt.isFinal);
        return object;
    }

    @Override
    public JsonElement visitWhileStmt(CompileStmt.While stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "while");
        object.add("condition", cache(stmt.condition));
        object.add("body", cache(stmt.body));
        //object.add("keyword", stmt.keyword.toJson());
        return object;
    }

    @Override
    public JsonElement visitForStmt(CompileStmt.For stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "for");
        object.add("init", cache(stmt.init));
        object.add("condition", cache(stmt.condition));
        object.add("increment", cache(stmt.increment));
        object.add("body", cache(stmt.body));
        //object.add("keyword", stmt.keyword.toJson());
        return object;
    }

    @Override
    public JsonElement visitForEachStmt(CompileStmt.ForEach stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "forEach");
        object.addProperty("type", stmt.type.absoluteName());
        object.addProperty("name", stmt.name.lexeme());
        object.add("init", cache(stmt.initializer));
        object.add("body", cache(stmt.body));
        return object;
    }

    @Override
    public JsonElement visitLoopInterruptionStmt(CompileStmt.LoopInterruption stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "loopInterrupt");
        object.addProperty("keyword", stmt.type.type().id());
        return object;
    }

    @Override
    public JsonElement visitTryStmt(CompileStmt.Try stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "try");
        object.add("body", cache(stmt.body));
        JsonArray array = new JsonArray();
        for (Pair<Pair<ClassReference[], Token>, CompileStmt.Block> pair : stmt.catches) {
            JsonObject pairDat = new JsonObject();
            Pair<ClassReference[], Token> pair1 = pair.left();
            JsonObject pair1Dat = new JsonObject();
            JsonArray classes = new JsonArray();
            Arrays.stream(pair1.left()).map(ClassReference::absoluteName).forEach(classes::add);
            pair1Dat.add("classes", classes);
            pair1Dat.addProperty("name", pair1.right().lexeme());
            pairDat.add("initData", pair1Dat);
            pairDat.add("executor", cache(pair.right()));
            array.add(pairDat);
        }
        object.add("catches", array);
        if (stmt.finale != null) object.add("finale", cache(stmt.finale));
        return object;
    }
}
