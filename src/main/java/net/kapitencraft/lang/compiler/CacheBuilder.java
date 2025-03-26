package net.kapitencraft.lang.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.holder.ast.CompileStmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.lang.tool.Util;

import java.util.Arrays;
import java.util.List;

public class CacheBuilder implements CompileExpr.Visitor<JsonElement>, CompileStmt.Visitor<JsonElement> {
    public JsonElement cache(CompileExpr expr) {
        return expr.accept(this);
    }

    public JsonElement cache(CompileStmt stmt) {
        return stmt.accept(this);
    }

    public JsonArray saveArgs(CompileExpr[] args) {
        JsonArray array = new JsonArray();
        Arrays.stream(args).map(this::cache).forEach(array::add);
        return array;
    }

    public JsonObject cacheClass(CacheableClass loxClass) {
        return loxClass.save(this);
    }

    public JsonArray cacheAnnotations(AnnotationClassInstance[] annotations) {
        JsonArray array = new JsonArray();
        for (AnnotationClassInstance instance : annotations) {
            //AnnotationClassInstance retention;
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
    public JsonElement visitAssignExpr(CompileExpr.Assign expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "assign");
        object.add("name", expr.name.toJson());
        object.add("value", cache(expr.value));
        object.add("type", expr.type.toJson());
        object.addProperty("executor", expr.executor.get().absoluteName());
        object.addProperty("operand", expr.operand.name());
        return object;
    }

    @Override
    public JsonElement visitSpecialAssignExpr(CompileExpr.SpecialAssign expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "specialAssign");
        object.add("name", expr.name.toJson());
        object.add("assignType", expr.assignType.toJson());
        return object;
    }

    @Override
    public JsonElement visitBinaryExpr(CompileExpr.Binary expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "binary");
        object.add("left", cache(expr.left));
        object.add("operator", expr.operator.toJson());
        object.addProperty("executor", expr.executor.absoluteName());
        object.addProperty("operand", expr.operand.name());
        object.add("right", cache(expr.right));
        return object;
    }

    @Override
    public JsonElement visitWhenExpr(CompileExpr.When expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "when");
        object.add("condition", cache(expr.condition));
        object.add("ifTrue", cache(expr.ifTrue));
        object.add("ifFalse", cache(expr.ifFalse));
        return object;
    }

    @Override
    public JsonElement visitInstCallExpr(CompileExpr.InstCall expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "instCall");
        object.add("callee", cache(expr.callee));
        object.add("name", expr.name.toJson());
        object.addProperty("ordinal", expr.methodOrdinal);
        object.add("args", saveArgs(expr.args));
        return object;
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
        object.add("name", expr.name.toJson());
        return object;
    }

    @Override
    public JsonElement visitStaticGetExpr(CompileExpr.StaticGet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "staticGet");
        object.addProperty("target", expr.target.absoluteName());
        object.add("name", expr.name.toJson());
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
        object.add("name", expr.name.toJson());
        object.add("value", cache(expr.value));
        object.add("assignType", expr.assignType.toJson());
        object.addProperty("executor", expr.executor.absoluteName());
        object.addProperty("operand", expr.operand.name());
        return object;
    }

    @Override
    public JsonElement visitStaticSetExpr(CompileExpr.StaticSet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "staticSet");
        object.addProperty("target", expr.target.absoluteName());
        object.add("name", expr.name.toJson());
        object.add("value", cache(expr.value));
        object.add("assignType", expr.assignType.toJson());
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
        object.add("assign", expr.assignType.toJson());
        object.addProperty("executor", expr.executor.absoluteName());
        object.addProperty("operand", expr.operand.name());
        return object;
    }

    @Override
    public JsonElement visitSpecialSetExpr(CompileExpr.SpecialSet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "specialSet");
        object.add("callee", cache(expr.callee));
        object.add("name", expr.name.toJson());
        object.add("assignType", expr.assignType.toJson());
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
    public JsonElement visitArraySpecialExpr(CompileExpr.ArraySpecial expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "specialArraySet");
        object.add("object", cache(expr.object));
        object.add("index", cache(expr.index));
        object.add("assign", expr.assignType.toJson());
        return object;
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
    public JsonElement visitSwitchExpr(CompileExpr.Switch expr) {
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
        if (expr.patternVarName != null) object.add("patternVarName", expr.patternVarName.toJson());
        object.addProperty("target", expr.targetType.absoluteName());
        return object;
    }

    @Override
    public JsonElement visitGroupingExpr(CompileExpr.Grouping expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "grouping");
        object.add("expr", cache(expr.expression));
        return object;
    }

    @Override
    public JsonElement visitLiteralExpr(CompileExpr.Literal expr) {
        JsonObject object = expr.token.toJson();
        object.addProperty("TYPE", "literal");
        return object;
    }

    @Override
    public JsonElement visitLogicalExpr(CompileExpr.Logical expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "logical");
        object.add("left", cache(expr.left));
        object.add("operator", expr.operator.toJson());
        object.add("right", cache(expr.right));
        return object;
    }

    @Override
    public JsonElement visitUnaryExpr(CompileExpr.Unary expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "unary");
        object.add("operator", expr.operator.toJson());
        object.add("arg", cache(expr.right));
        return object;
    }

    @Override
    public JsonElement visitVarRefExpr(CompileExpr.VarRef expr) {
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
        object.add("keyword", expr.keyword.toJson());
        object.addProperty("ordinal", expr.ordinal);
        return object;
    }

    @Override
    public JsonElement visitBlockStmt(CompileStmt.Block stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "block");
        JsonArray array = new JsonArray();
        Arrays.stream(stmt.statements).map(this::cache).forEach(array::add);
        object.add("statements", array);
        return object;
    }

    @Override
    public JsonElement visitExpressionStmt(CompileStmt.Expression stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "expression");
        object.add("expr", cache(stmt.expression));
        return object;
    }

    @Override
    public JsonElement visitIfStmt(CompileStmt.If stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "if");
        object.add("condition", cache(stmt.condition));
        object.add("then", cache(stmt.thenBranch));
        if (stmt.elseBranch != null) object.add("elseBranch", cache(stmt.elseBranch));
        JsonArray array = new JsonArray();
        Arrays.stream(stmt.elifs).map(pair -> {
            JsonObject object1 = new JsonObject();
            object1.add("condition", cache(pair.left()));
            object1.add("body", cache(pair.right()));
            return object1;
        }).forEach(array::add);
        object.add("elifs", array);
        object.add("keyword", stmt.keyword.toJson());
        return object;
    }

    @Override
    public JsonElement visitReturnStmt(CompileStmt.Return stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "return");
        object.add("keyword", stmt.keyword.toJson());
        if (stmt.value != null) object.add("value", cache(stmt.value));
        return object;
    }

    @Override
    public JsonElement visitThrowStmt(CompileStmt.Throw stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "throw");
        object.add("keyword", stmt.keyword.toJson());
        object.add("value", cache(stmt.value));
        return object;
    }

    @Override
    public JsonElement visitVarDeclStmt(CompileStmt.VarDecl stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "varDecl");
        object.add("name", stmt.name.toJson());
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
        object.add("keyword", stmt.keyword.toJson());
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
        object.add("keyword", stmt.keyword.toJson());
        return object;
    }

    @Override
    public JsonElement visitForEachStmt(CompileStmt.ForEach stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "forEach");
        object.addProperty("type", stmt.type.absoluteName());
        object.add("name", stmt.name.toJson());
        object.add("init", cache(stmt.initializer));
        object.add("body", cache(stmt.body));
        return object;
    }

    @Override
    public JsonElement visitLoopInterruptionStmt(CompileStmt.LoopInterruption stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "loopInterrupt");
        object.add("keyword", stmt.type.toJson());
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
            pair1Dat.add("name", pair1.right().toJson());
            pairDat.add("initData", pair1Dat);
            pairDat.add("executor", cache(pair.right()));
            array.add(pairDat);
        }
        object.add("catches", array);
        if (stmt.finale != null) object.add("finale", cache(stmt.finale));
        return object;
    }
}
