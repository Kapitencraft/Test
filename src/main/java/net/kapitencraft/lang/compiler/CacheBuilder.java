package net.kapitencraft.lang.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.GeneratedClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.tool.Util;

import java.util.List;

public class CacheBuilder implements Expr.Visitor<JsonElement>, Stmt.Visitor<JsonElement> {
    public JsonElement cache(Expr expr) {
        return expr.accept(this);
    }

    public JsonElement cache(Stmt stmt) {
        return stmt.accept(this);
    }

    private JsonArray saveArgs(List<Expr> args) {
        JsonArray array = new JsonArray();
        args.stream().map(this::cache).forEach(array::add);
        return array;
    }

    public JsonObject cacheClass(CacheableClass loxClass) {
        return loxClass.save(this);
    }

    @Override
    public JsonElement visitAssignExpr(Expr.Assign expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "assign");
        object.add("name", expr.name.toJson());
        object.add("value", cache(expr.value));
        object.add("type", expr.type.toJson());
        return object;
    }

    @Override
    public JsonElement visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "specialAssign");
        object.add("name", expr.name.toJson());
        object.add("assignType", expr.assignType.toJson());
        return object;
    }

    @Override
    public JsonElement visitBinaryExpr(Expr.Binary expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "binary");
        object.add("left", cache(expr.left));
        object.add("operator", expr.operator.toJson());
        object.add("right", cache(expr.right));
        return object;
    }

    @Override
    public JsonElement visitWhenExpr(Expr.When expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "when");
        object.add("condition", cache(expr.condition));
        object.add("ifTrue", cache(expr.ifTrue));
        object.add("ifFalse", cache(expr.ifFalse));
        return object;
    }

    @Override
    public JsonElement visitInstCallExpr(Expr.InstCall expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "instCall");
        object.add("callee", cache(expr.callee));
        object.add("name", expr.name.toJson());
        object.addProperty("ordinal", expr.methodOrdinal);
        object.add("args", saveArgs(expr.args));
        return object;
    }

    @Override
    public JsonElement visitStaticCallExpr(Expr.StaticCall expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "staticCall");
        object.addProperty("target", expr.target.absoluteName());
        object.add("name", expr.name.toJson());
        object.addProperty("ordinal", expr.methodOrdinal);
        object.add("args", saveArgs(expr.args));
        return object;
    }

    @Override
    public JsonElement visitGetExpr(Expr.Get expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "get");
        object.add("callee", cache(expr.object));
        object.add("name", expr.name.toJson());
        return object;
    }

    @Override
    public JsonElement visitStaticGetExpr(Expr.StaticGet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("target", expr.target.absoluteName());
        object.add("name", expr.name.toJson());
        return object;
    }

    @Override
    public JsonElement visitArrayGetExpr(Expr.ArrayGet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "arrayGet");
        object.add("object", cache(expr.object));
        object.add("index", cache(expr.index));
        return object;
    }

    @Override
    public JsonElement visitSetExpr(Expr.Set expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "set");
        object.add("callee", cache(expr.object));
        object.add("name", expr.name.toJson());
        object.add("value", cache(expr.value));
        object.add("assignType", expr.assignType.toJson());
        return object;
    }

    @Override
    public JsonElement visitStaticSetExpr(Expr.StaticSet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "staticSet");
        object.addProperty("target", expr.target.absoluteName());
        object.add("name", expr.name.toJson());
        object.add("value", cache(expr.value));
        object.add("assignType", expr.assignType.toJson());
        return object;
    }

    @Override
    public JsonElement visitArraySetExpr(Expr.ArraySet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "arraySet");
        object.add("object", cache(expr.object));
        object.add("index", cache(expr.index));
        object.add("assign", expr.assignType.toJson());
        return object;
    }

    @Override
    public JsonElement visitSpecialSetExpr(Expr.SpecialSet expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "specialSet");
        object.add("callee", cache(expr.callee));
        object.add("name", expr.name.toJson());
        object.add("assignType", expr.assignType.toJson());
        return object;
    }

    @Override
    public JsonElement visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "specialStaticSet");
        object.addProperty("target", expr.target.absoluteName());
        object.add("name", expr.name.toJson());
        object.add("assignType", expr.assignType.toJson());
        return object;
    }

    @Override
    public JsonElement visitArraySpecialExpr(Expr.ArraySpecial expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "specialArraySet");
        object.add("object", cache(expr.object));
        object.add("index", cache(expr.index));
        object.add("assign", expr.assignType.toJson());
        return object;
    }

    @Override
    public JsonElement visitSwitchExpr(Expr.Switch expr) {
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
    public JsonElement visitCastCheckExpr(Expr.CastCheck expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "castCheck");
        object.add("object", cache(expr.object));
        if (expr.patternVarName != null) object.add("patternVarName", expr.patternVarName.toJson());
        object.addProperty("target", expr.targetType.absoluteName());
        return object;
    }

    @Override
    public JsonElement visitGroupingExpr(Expr.Grouping expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "grouping");
        object.add("expr", cache(expr.expression));
        return object;
    }

    @Override
    public JsonElement visitLiteralExpr(Expr.Literal expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "literal");
        object.add("value", expr.value.toJson());
        return object;
    }

    @Override
    public JsonElement visitLogicalExpr(Expr.Logical expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "logical");
        object.add("left", cache(expr.left));
        object.add("operator", expr.operator.toJson());
        object.add("right", cache(expr.right));
        return object;
    }

    @Override
    public JsonElement visitUnaryExpr(Expr.Unary expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "unary");
        object.add("operator", expr.operator.toJson());
        object.add("arg", cache(expr.right));
        return object;
    }

    @Override
    public JsonElement visitVarRefExpr(Expr.VarRef expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "varRef");
        object.add("name", expr.name.toJson());
        return object;
    }

    @Override
    public JsonElement visitConstructorExpr(Expr.Constructor expr) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "constructors");
        object.addProperty("target", expr.target.absoluteName());
        object.add("args", saveArgs(expr.params));
        object.add("keyword", expr.keyword.toJson());
        object.addProperty("ordinal", expr.ordinal);
        return object;
    }

    @Override
    public JsonElement visitBlockStmt(Stmt.Block stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "block");
        JsonArray array = new JsonArray();
        stmt.statements.stream().map(this::cache).forEach(array::add);
        object.add("statements", array);
        return object;
    }

    @Override
    public JsonElement visitExpressionStmt(Stmt.Expression stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "expression");
        object.add("expr", cache(stmt.expression));
        return object;
    }

    @Override
    public JsonElement visitIfStmt(Stmt.If stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "if");
        object.add("condition", cache(stmt.condition));
        object.add("then", cache(stmt.thenBranch));
        if (stmt.elseBranch != null) object.add("elseBranch", cache(stmt.elseBranch));
        JsonArray array = new JsonArray();
        stmt.elifs.stream().map(pair -> {
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
    public JsonElement visitReturnStmt(Stmt.Return stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "return");
        object.add("keyword", stmt.keyword.toJson());
        if (stmt.value != null) object.add("value", cache(stmt.value));
        return object;
    }

    @Override
    public JsonElement visitThrowStmt(Stmt.Throw stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "throw");
        object.add("keyword", stmt.keyword.toJson());
        object.add("value", cache(stmt.value));
        return object;
    }

    @Override
    public JsonElement visitVarDeclStmt(Stmt.VarDecl stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "varDecl");
        object.add("name", stmt.name.toJson());
        object.addProperty("targetType", stmt.type.absoluteName());
        if (stmt.initializer != null) object.add("initializer", cache(stmt.initializer));
        object.addProperty("isFinal", stmt.isFinal);
        return object;
    }

    @Override
    public JsonElement visitWhileStmt(Stmt.While stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "while");
        object.add("condition", cache(stmt.condition));
        object.add("body", cache(stmt.body));
        object.add("keyword", stmt.keyword.toJson());
        return object;
    }

    @Override
    public JsonElement visitForStmt(Stmt.For stmt) {
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
    public JsonElement visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "loopInterrupt");
        object.add("keyword", stmt.type.toJson());
        return object;
    }

    @Override
    public JsonElement visitTryStmt(Stmt.Try stmt) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "try");
        object.add("body", cache(stmt.body));
        JsonArray array = new JsonArray();
        stmt.catches.forEach(pair -> {
            JsonObject pairDat = new JsonObject();
            Pair<List<LoxClass>, Token> pair1 = pair.left();
            JsonObject pair1Dat = new JsonObject();
            JsonArray classes = new JsonArray();
            pair1.left().stream().map(LoxClass::absoluteName).forEach(classes::add);
            pair1Dat.add("classes", classes);
            pair1Dat.add("name", pair1.right().toJson());
            pairDat.add("initData", pair1Dat);
            pairDat.add("executor", cache(pair.right()));
            array.add(pairDat);
        });
        object.add("catches", array);
        if (stmt.finale != null) object.add("finale", cache(stmt.finale));
        return object;
    }
}
