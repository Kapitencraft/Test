package net.kapitencraft.lang.run;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.tool.Util;

import java.util.List;

public class CacheLoader {

    private static List<Expr> readArgs(JsonObject object, String name) {
        return GsonHelper.getAsJsonArray(object, name).asList().stream().map(JsonElement::getAsJsonObject).map(CacheLoader::readExpr).toList();
    }

    public static List<Stmt> readStmtList(JsonObject object, String name) {
        return GsonHelper.getAsJsonArray(object, name).asList().stream().map(JsonElement::getAsJsonObject).map(CacheLoader::readStmt).toList();
    }

    public static Expr readSubExpr(JsonObject object, String name) {
        return readExpr(GsonHelper.getAsJsonObject(object, name));
    }

    //TODO move to registry after merge
    public static Expr readExpr(JsonObject object) {
        String type = GsonHelper.getAsString(object, "TYPE");
        return switch (type) {
            case "assign" -> readAssign(object);
            case "specialAssign" -> readSpecialAssign(object);
            case "binary" -> readBinary(object);
            case "when" -> readWhen(object);
            case "call" -> readCall(object); //removed soon
            case "instCall" -> readInstCall(object);
            case "get" -> readGet(object);
            case "set" -> readSet(object);
            case "specialSet" -> readSpecialSet(object);
            case "switch" -> readSwitch(object);
            case "castCheck" -> readCastCheck(object);
            case "grouping" -> readGrouping(object);
            case "literal" -> readLiteral(object);
            case "logical" -> readLogical(object);
            case "unary" -> readUnary(object);
            case "varRef" -> readVarRef(object);
            case "funcRef" -> readFuncRef(object);
            case "constructor" -> readConstructor(object);
            default -> throw new IllegalStateException("unknown expr key '" + type + "'");
        };
    }

    private static Expr readBinary(JsonObject object) {
        Expr left = readSubExpr(object, "left");
        Token operator = Token.readFromSubObject(object, "operator");
        Expr right = readSubExpr(object, "right");
        return new Expr.Binary(left, operator, right);
    }

    private static Expr readWhen(JsonObject object) {
        Expr condition = readSubExpr(object, "condition");
        Expr ifTrue = readSubExpr(object, "ifTrue");
        Expr ifFalse = readSubExpr(object, "ifFalse");
        return new Expr.When(condition, ifTrue, ifFalse);
    }

    private static Expr readCall(JsonObject object) {
        Expr callee = readSubExpr(object, "callee");
        Token bracket = Token.readFromSubObject(object, "bracket");
        List<Expr> args = readArgs(object, "args");
        return new Expr.Call(callee, bracket, args);
    }

    private static Expr readInstCall(JsonObject object) {
        Expr callee = readSubExpr(object, "callee");
        Token name = Token.readFromSubObject(object, "name");
        int ordinal = GsonHelper.getAsInt(object, "ordinal");
        Token bracket = Token.readFromSubObject(object, "bracket");
        List<Expr> args = readArgs(object, "args");
        return new Expr.InstCall(callee, name, ordinal, bracket, args);
    }

    private static Expr readGet(JsonObject object) {
        Expr callee = readSubExpr(object, "callee");
        Token name = Token.readFromSubObject(object, "name");
        return new Expr.Get(callee, name);
    }

    private static Expr readSet(JsonObject object) {
        Expr callee = readSubExpr(object, "callee");
        Token name = Token.readFromSubObject(object, "name");
        Expr value = readSubExpr(object, "value");
        Token assignType = Token.readFromSubObject(object, "assignType");
        return new Expr.Set(callee, name, value, assignType);
    }

    private static Expr readSpecialSet(JsonObject object) {
        Expr callee = readSubExpr(object, "callee");
        Token name = Token.readFromSubObject(object, "name");
        Token assignType = Token.readFromSubObject(object, "assignType");
        return new Expr.SpecialSet(callee, name, assignType);
    }

    private static Expr readSwitch(JsonObject object) {
        Expr provider = readSubExpr(object, "provider");
        Expr defaulted = readSubExpr(object, "defaulted");
        Token keyword = Token.readFromSubObject(object, "keyword");
        return new Expr.Switch(provider, Util.readMap(
                GsonHelper.getAsJsonArray(object, "elements"),
                (object1, name) -> {
                    JsonPrimitive primitive = object1.getAsJsonPrimitive(name);
                    if (primitive.isBoolean()) return primitive.getAsBoolean();
                    else if (primitive.isNumber()) return primitive.getAsNumber();
                    else if (primitive.isString()) return primitive.getAsString();
                    return primitive.getAsCharacter();
                },
                CacheLoader::readSubExpr
        ), defaulted, keyword);
    }

    private static Expr readCastCheck(JsonObject object) {
        Expr obj = readSubExpr(object, "object");
        Token patternVarName = object.has("patternVarName") ? Token.readFromSubObject(object, "patternVarName") : null;
        LoxClass target = ClassLoader.loadClassReference(object, "target");
        return new Expr.CastCheck(obj, target, patternVarName);
    }

    private static Expr readGrouping(JsonObject object) {
        return new Expr.Grouping(readSubExpr(object, "expr"));
    }

    private static Expr readLiteral(JsonObject object) {
        return new Expr.Literal(Token.readFromSubObject(object, "value"));
    }

    private static Expr readLogical(JsonObject object) {
        Expr left = readSubExpr(object, "left");
        Token operator = Token.readFromSubObject(object, "operator");
        Expr right = readSubExpr(object, "right");
        return new Expr.Logical(left, operator, right);
    }

    private static Expr readUnary(JsonObject object) {
        Token operator = Token.readFromSubObject(object, "operator");
        Expr arg = readSubExpr(object, "arg");
        return new Expr.Unary(operator, arg);
    }

    private static Expr readVarRef(JsonObject object) {
        return new Expr.VarRef(Token.readFromSubObject(object, "name"));
    }

    private static Expr readFuncRef(JsonObject object) {
        return new Expr.FuncRef(Token.readFromSubObject(object, "name"));
    }

    private static Expr readConstructor(JsonObject object) {
        LoxClass target = ClassLoader.loadClassReference(object, "target");
        List<Expr> args = readArgs(object, "args");
        int ordinal = GsonHelper.getAsInt(object, "ordinal");
        return new Expr.Constructor(target, args, ordinal);
    }

    private static Expr readAssign(JsonObject object) {
        Token name = Token.readFromSubObject(object, "name");
        Expr value = readSubExpr(object, "value");
        Token type = Token.readFromSubObject(object, "type");
        return new Expr.Assign(name, value, type);
    }

    private static Expr readSpecialAssign(JsonObject object) {
        Token name = Token.readFromSubObject(object, "name");
        Token assignType = Token.readFromSubObject(object, "assignType");
        return new Expr.SpecialAssign(name, assignType);
    }

    public static Stmt readStmt(JsonObject object) {
        String type = GsonHelper.getAsString(object, "TYPE");
        return switch (type) {
            case "block" -> readBlock(object);
            case "expression" -> readExpression(object);
            case "if" -> readIf(object);
            case "return" -> readReturn(object);
            case "varDecl" -> readVarDecl(object);
            case "while" -> readWhile(object);
            case "for" -> readFor(object);
            case "loopInterrupt" -> readLoopInterrupt(object);
            default -> throw new IllegalStateException("unknown stmt key '" + type + "'");
        };
    }

    private static Stmt readBlock(JsonObject object) {
        return new Stmt.Block(readStmtList(object, "statements"));
    }

    private static Stmt readExpression(JsonObject object) {
        return new Stmt.Expression(readSubExpr(object, "expr"));
    }

    private static Stmt readIf(JsonObject object) {
        Expr condition = readSubExpr(object, "condition");
        Stmt then = readStmt(GsonHelper.getAsJsonObject(object, "then"));
        Stmt elseBranch = object.has("elseBranch") ? readStmt(GsonHelper.getAsJsonObject(object, "elseBranch")) : null;
        JsonArray elifData = GsonHelper.getAsJsonArray(object, "elifs");
        List<Pair<Expr, Stmt>> elifs = elifData.asList().stream().map(JsonElement::getAsJsonObject).map(object1 -> {
            Expr pairCondition = readSubExpr(object1, "condition");
            Stmt pairBody = readStmt(GsonHelper.getAsJsonObject(object1, "body"));
            return Pair.of(pairCondition, pairBody);
        }).toList();
        Token keyword = Token.readFromSubObject(object, "keyword");
        return new Stmt.If(condition, then, elseBranch, elifs, keyword);
    }

    private static Stmt readReturn(JsonObject object) {
        Token keyword = Token.readFromSubObject(object, "keyword");
        Expr value = object.has("value") ? readSubExpr(object, "value") : null;
        return new Stmt.Return(keyword, value);
    }

    private static Stmt readVarDecl(JsonObject object) {
        Token name = Token.readFromSubObject(object, "name");
        LoxClass type = ClassLoader.loadClassReference(object, "targetType");
        Expr init = object.has("initializer") ? readSubExpr(object, "initializer") : null;
        boolean isFinal = GsonHelper.getAsBoolean(object, "isFinal");
        return new Stmt.VarDecl(name, type, init, isFinal);
    }

    private static Stmt readWhile(JsonObject object) {
        Expr condition = readSubExpr(object, "condition");
        Stmt body = readStmt(GsonHelper.getAsJsonObject(object, "stmt"));
        Token keyword = Token.readFromSubObject(object, "keyword");
        return new Stmt.While(condition, body, keyword);
    }

    private static Stmt readFor(JsonObject object) {
        Stmt init = readStmt(GsonHelper.getAsJsonObject(object, "init"));
        Expr condition = readSubExpr(object, "condition");
        Expr increment = readSubExpr(object, "increment");
        Stmt body = readStmt(GsonHelper.getAsJsonObject(object, "body"));
        Token keyword = Token.readFromSubObject(object, "keyword");
        return new Stmt.For(init, condition, increment, body, keyword);
    }

    private static Stmt readLoopInterrupt(JsonObject object) {
        return new Stmt.LoopInterruption(Token.readFromSubObject(object, "keyword"));
    }
}