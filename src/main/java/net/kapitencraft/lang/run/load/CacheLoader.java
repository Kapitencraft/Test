package net.kapitencraft.lang.run.load;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kapitencraft.lang.holder.LiteralHolder;
import net.kapitencraft.lang.holder.ast.RuntimeExpr;
import net.kapitencraft.lang.holder.ast.RuntimeStmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.RuntimeToken;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.lang.tool.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheLoader {

    public static RuntimeExpr[] readArgs(JsonObject object, String name) {
        return GsonHelper.getAsJsonArray(object, name).asList().stream().map(JsonElement::getAsJsonObject).map(CacheLoader::readExpr).toArray(RuntimeExpr[]::new);
    }

    public static RuntimeStmt[] readStmtList(JsonObject object, String name) {
        return GsonHelper.getAsJsonArray(object, name).asList().stream().map(JsonElement::getAsJsonObject).map(CacheLoader::readStmt).toArray(RuntimeStmt[]::new);
    }

    private static RuntimeExpr readSubExpr(JsonObject object, String name) {
        return readExpr(GsonHelper.getAsJsonObject(object, name));
    }

    public static RuntimeExpr readOptionalSubExpr(JsonObject object, String name) {
        return object.has(name) ? readSubExpr(object, name) : null;
    }

    public static RuntimeExpr readExpr(JsonObject object) {
        String type = GsonHelper.getAsString(object, "TYPE");
        return switch (type) {
            case "assign" -> readAssign(object);
            case "specialAssign" -> readSpecialAssign(object);
            case "binary" -> readBinary(object);
            case "when" -> readWhen(object);
            case "instCall" -> readInstCall(object);
            case "staticCall" -> readStaticCall(object);
            case "get" -> readGet(object);
            case "staticGet" -> readStaticGet(object);
            case "arrayGet" -> readArrayGet(object);
            case "set" -> readSet(object);
            case "staticSet" -> readStaticSet(object);
            case "arraySet" -> readArraySet(object);
            case "specialSet" -> readSpecialSet(object);
            case "specialStaticSet" -> readSpecialStaticSet(object);
            case "specialArraySet" -> readSpecialArraySet(object);
            case "slice" -> readSlice(object);
            case "switch" -> readSwitch(object);
            case "castCheck" -> readCastCheck(object);
            case "grouping" -> readGrouping(object);
            case "literal" -> readLiteral(object);
            case "logical" -> readLogical(object);
            case "unary" -> readUnary(object);
            case "varRef" -> readVarRef(object);
            case "constructors" -> readConstructor(object);
            default -> throw new IllegalStateException("unknown expr key '" + type + "'");
        };
    }

    private static RuntimeExpr readSlice(JsonObject object) {
        RuntimeExpr obj = readSubExpr(object, "object");
        RuntimeExpr start = readOptionalSubExpr(object, "start");
        RuntimeExpr end = readOptionalSubExpr(object, "end");
        RuntimeExpr interval = readOptionalSubExpr(object, "interval");
        return new RuntimeExpr.Slice(obj, start, end, interval);
    }

    private static RuntimeExpr readArraySet(JsonObject object) {
        RuntimeExpr obj = readSubExpr(object, "object");
        RuntimeExpr index = readSubExpr(object, "index");
        RuntimeExpr value = readSubExpr(object, "value");
        TokenType assign = TokenType.readFromSubObject(object, "assign");
        ClassReference executor = ClassLoader.loadClassReference(object, "executor");
        Operand operand = Operand.fromJson(object, "operand");
        return new RuntimeExpr.ArraySet(obj, index, value, assign, executor, operand);
    }

    private static RuntimeExpr readSpecialArraySet(JsonObject object) {
        RuntimeExpr obj = readSubExpr(object, "object");
        RuntimeExpr index = readSubExpr(object, "index");
        TokenType assign = TokenType.readFromSubObject(object, "assign");
        return new RuntimeExpr.ArraySpecial(obj, index, assign);
    }

    private static RuntimeExpr readArrayGet(JsonObject object) {
        RuntimeExpr obj = readSubExpr(object, "object");
        RuntimeExpr index = readSubExpr(object, "index");
        return new RuntimeExpr.ArrayGet(obj, index);
    }

    private static RuntimeExpr readStaticGet(JsonObject object) {
        ClassReference target = ClassLoader.loadClassReference(object, "target");
        String name = GsonHelper.getAsString(object, "name");
        return new RuntimeExpr.StaticGet(target, name);
    }

    private static RuntimeExpr readStaticSet(JsonObject object) {
        ClassReference target = ClassLoader.loadClassReference(object, "target");
        String name = GsonHelper.getAsString(object, "name");
        RuntimeExpr value = readSubExpr(object, "value");
        TokenType assignType = TokenType.readFromSubObject(object, "assignType");
        ClassReference executor = ClassLoader.loadClassReference(object, "executor");
        Operand operand = Operand.fromJson(object, "operand");
        return new RuntimeExpr.StaticSet(target, name, value, assignType, executor, operand);

    }

    private static RuntimeExpr readSpecialStaticSet(JsonObject object) {
        ClassReference target = ClassLoader.loadClassReference(object, "target");
        String name = GsonHelper.getAsString(object, "name");
        TokenType assignType = TokenType.readFromSubObject(object, "assignType");
        return new RuntimeExpr.StaticSpecial(target, name, assignType);
    }

    private static RuntimeExpr readStaticCall(JsonObject object) {
        ClassReference target = ClassLoader.loadClassReference(object, "target");
        RuntimeToken name = RuntimeToken.readFromSubObject(object, "name");
        int ordinal = GsonHelper.getAsInt(object, "ordinal");
        RuntimeExpr[] args = readArgs(object, "args");
        return null; //new RuntimeExpr.StaticCall(target, name, ordinal, args);
    }

    private static RuntimeExpr readBinary(JsonObject object) {
        RuntimeExpr left = readSubExpr(object, "left");
        TokenType operator = TokenType.readFromSubObject(object, "operator");
        ClassReference executor = ClassLoader.loadClassReference(object, "executor");
        Operand operand = Operand.fromJson(object, "operand");
        RuntimeExpr right = readSubExpr(object, "right");
        return new RuntimeExpr.Binary(left, right, operator, executor, operand);
    }

    private static RuntimeExpr readWhen(JsonObject object) {
        RuntimeExpr condition = readSubExpr(object, "condition");
        RuntimeExpr ifTrue = readSubExpr(object, "ifTrue");
        RuntimeExpr ifFalse = readSubExpr(object, "ifFalse");
        return new RuntimeExpr.When(condition, ifTrue, ifFalse);
    }

    private static RuntimeExpr readInstCall(JsonObject object) {
        RuntimeExpr callee = readSubExpr(object, "callee");
        RuntimeToken name = RuntimeToken.readFromSubObject(object, "name");
        int ordinal = GsonHelper.getAsInt(object, "ordinal");
        RuntimeExpr[] args = readArgs(object, "args");
        return null; //new RuntimeExpr.InstCall(callee, name, ordinal, args);
    }

    private static RuntimeExpr readGet(JsonObject object) {
        RuntimeExpr callee = readSubExpr(object, "callee");
        String name = GsonHelper.getAsString(object, "name");
        return new RuntimeExpr.Get(callee, name);
    }

    private static RuntimeExpr readSet(JsonObject object) {
        RuntimeExpr callee = readSubExpr(object, "callee");
        String name = GsonHelper.getAsString(object, "name");
        RuntimeExpr value = readSubExpr(object, "value");
        TokenType assignType = TokenType.readFromSubObject(object, "assignType");
        ClassReference executor = ClassLoader.loadClassReference(object, "executor");
        Operand operand = Operand.fromJson(object, "operand");
        return new RuntimeExpr.Set(callee, name, value, assignType, executor, operand);
    }

    private static RuntimeExpr readSpecialSet(JsonObject object) {
        RuntimeExpr callee = readSubExpr(object, "callee");
        String name = GsonHelper.getAsString(object, "name");
        TokenType assignType = TokenType.readFromSubObject(object, "assignType");
        return new RuntimeExpr.SpecialSet(callee, name, assignType);
    }

    @SuppressWarnings("deprecation")
    private static RuntimeExpr readSwitch(JsonObject object) {
        return null;
        //RuntimeExpr provider = readSubExpr(object, "provider");
        //RuntimeExpr defaulted = readSubExpr(object, "defaulted");
        //return new RuntimeExpr.Switch(provider, Util.readMap(
        //        GsonHelper.getAsJsonArray(object, "elements"),
        //        (object1, name) -> {
        //            JsonPrimitive primitive = object1.getAsJsonPrimitive(name);
        //            if (primitive.isBoolean()) return primitive.getAsBoolean();
        //            else if (primitive.isNumber()) return primitive.getAsNumber();
        //            else if (primitive.isString()) return primitive.getAsString();
        //            return primitive.getAsCharacter();
        //        },
        //        CacheLoader::readSubExpr
        //), defaulted);
    }

    private static RuntimeExpr readCastCheck(JsonObject object) {
        RuntimeExpr obj = readSubExpr(object, "object");
        String patternVarName = object.has("patternVarName") ? GsonHelper.getAsString(object, "patternVarName") : null;
        ClassReference target = ClassLoader.loadClassReference(object, "target");
        return new RuntimeExpr.CastCheck(obj, target, patternVarName);
    }

    private static RuntimeExpr readGrouping(JsonObject object) {
        return new RuntimeExpr.Grouping(readSubExpr(object, "expr"));
    }

    private static RuntimeExpr readLiteral(JsonObject object) {
        return new RuntimeExpr.Literal(LiteralHolder.fromJson(GsonHelper.getAsJsonObject(object, "literal")));
    }

    private static RuntimeExpr readLogical(JsonObject object) {
        RuntimeExpr left = readSubExpr(object, "left");
        TokenType operator = TokenType.readFromSubObject(object, "operator");
        RuntimeExpr right = readSubExpr(object, "right");
        return new RuntimeExpr.Logical(left, operator, right);
    }

    private static RuntimeExpr readUnary(JsonObject object) {
        TokenType operator = TokenType.readFromSubObject(object, "operator");
        RuntimeExpr arg = readSubExpr(object, "arg");
        return new RuntimeExpr.Unary(operator, arg);
    }

    private static RuntimeExpr readVarRef(JsonObject object) {
        return new RuntimeExpr.VarRef(GsonHelper.getAsByte(object, "ordinal"));
    }

    private static RuntimeExpr readConstructor(JsonObject object) {
        ClassReference target = ClassLoader.loadClassReference(object, "target");
        RuntimeExpr[] args = readArgs(object, "args");
        int ordinal = GsonHelper.getAsInt(object, "ordinal");
        return null; //new RuntimeExpr.Constructor(target, args, ordinal);
    }

    private static RuntimeExpr readAssign(JsonObject object) {
        RuntimeToken name = RuntimeToken.readFromSubObject(object, "name");
        RuntimeExpr value = readSubExpr(object, "value");
        TokenType type = TokenType.readFromSubObject(object, "type");
        int line = GsonHelper.getAsInt(object, "line");
        ClassReference executor = ClassLoader.loadClassReference(object, "executor");
        Operand operand = Operand.valueOf(GsonHelper.getAsString(object, "operand"));
        return null; //new RuntimeExpr.Assign(name, value, type, line, executor, operand);
    }

    private static RuntimeExpr readSpecialAssign(JsonObject object) {
        RuntimeToken name = RuntimeToken.readFromSubObject(object, "name");
        TokenType assignType = TokenType.readFromSubObject(object, "assignType");
        return new RuntimeExpr.SpecialAssign(name, assignType);
    }

    public static RuntimeStmt readStmt(JsonObject object) {
        String type = GsonHelper.getAsString(object, "TYPE");
        return switch (type) {
            case "block" -> readBlock(object);
            case "expression" -> readExpression(object);
            case "if" -> readIf(object);
            case "return" -> readReturn(object);
            case "throw" -> readThrow(object);
            case "varDecl" -> readVarDecl(object);
            case "while" -> readWhile(object);
            case "for" -> readFor(object);
            case "forEach" -> readForEach(object);
            case "try" -> readTry(object);
            case "loopInterrupt" -> readLoopInterrupt(object);
            default -> throw new IllegalStateException("unknown stmt key '" + type + "'");
        };
    }

    private static RuntimeStmt readForEach(JsonObject object) {
        ClassReference type = ClassLoader.loadClassReference(object, "type");
        String name = GsonHelper.getAsString(object, "name");
        RuntimeExpr init = readSubExpr(object, "init");
        RuntimeStmt body = readStmt(GsonHelper.getAsJsonObject(object, "body"));
        return new RuntimeStmt.ForEach(type, name, init, body);
    }

    private static RuntimeStmt readTry(JsonObject object) {
        RuntimeStmt.Block body = readBlock(GsonHelper.getAsJsonObject(object, "body"));
        List<Pair<Pair<List<ClassReference>, Token>, RuntimeStmt.Block>> catches = new ArrayList<>();
        {
            GsonHelper.getAsJsonArray(object, "catches").asList().stream()
                    .map(JsonElement::getAsJsonObject)
                    .forEach(obj -> {
                        JsonObject initData = GsonHelper.getAsJsonObject(obj, "initData");
                        List<ClassReference> targets = GsonHelper.getAsJsonArray(initData, "classes")
                                .asList()
                                .stream()
                                .map(JsonElement::getAsString)
                                .map(VarTypeManager::getClassOrError)
                                .toList();
                        Token name = Token.readFromSubObject(initData, "name");
                        RuntimeStmt.Block block = readBlock(GsonHelper.getAsJsonObject(obj, "executor"));

                        catches.add(Pair.of(
                                Pair.of(
                                        targets,
                                        name
                                ),
                                block
                        ));
                    });
            ;
        }
        RuntimeStmt.Block finale = null;
        if (object.has("finale")) finale = readBlock(GsonHelper.getAsJsonObject(object, "finale"));
        return new RuntimeStmt.Try(body, catches.toArray(Pair[]::new), finale);
    }

    private static RuntimeStmt readThrow(JsonObject object) {
        int line = GsonHelper.getAsInt(object, "line");
        RuntimeExpr expr = readSubExpr(object, "value");
        return null;// new RuntimeStmt.Throw(line, expr);
    }

    private static RuntimeStmt.Block readBlock(JsonObject object) {
        return new RuntimeStmt.Block(readStmtList(object, "statements"));
    }

    private static RuntimeStmt readExpression(JsonObject object) {
        return new RuntimeStmt.Expression(readSubExpr(object, "expr"));
    }

    private static RuntimeStmt readIf(JsonObject object) {
        RuntimeExpr condition = readSubExpr(object, "condition");
        RuntimeStmt then = readStmt(GsonHelper.getAsJsonObject(object, "then"));
        RuntimeStmt elseBranch = object.has("elseBranch") ? readStmt(GsonHelper.getAsJsonObject(object, "elseBranch")) : null;
        JsonArray elifData = GsonHelper.getAsJsonArray(object, "elifs");
        List<Pair<RuntimeExpr, RuntimeStmt>> elifs = elifData.asList().stream().map(JsonElement::getAsJsonObject).map(object1 -> {
            RuntimeExpr pairCondition = readSubExpr(object1, "condition");
            RuntimeStmt pairBody = readStmt(GsonHelper.getAsJsonObject(object1, "body"));
            return Pair.of(pairCondition, pairBody);
        }).toList();
        return null;// new RuntimeStmt.If(condition, then, elseBranch, elifs.toArray(Pair[]::new));
    }

    private static RuntimeStmt readReturn(JsonObject object) {
        RuntimeExpr value = readOptionalSubExpr(object, "value");
        return null;// new RuntimeStmt.Return(value);
    }

    private static RuntimeStmt readVarDecl(JsonObject object) {
        String name = GsonHelper.getAsString(object, "name");
        ClassReference type = ClassLoader.loadClassReference(object, "targetType");
        RuntimeExpr init = readOptionalSubExpr(object, "initializer");
        boolean isFinal = GsonHelper.getAsBoolean(object, "isFinal");
        return new RuntimeStmt.VarDecl(name, type, init, isFinal);
    }

    private static RuntimeStmt readWhile(JsonObject object) {
        RuntimeExpr condition = readSubExpr(object, "condition");
        RuntimeStmt body = readStmt(GsonHelper.getAsJsonObject(object, "body"));
        return null;// new RuntimeStmt.While(condition, body);
    }

    private static RuntimeStmt readFor(JsonObject object) {
        RuntimeStmt init = readStmt(GsonHelper.getAsJsonObject(object, "init"));
        RuntimeExpr condition = readSubExpr(object, "condition");
        RuntimeExpr increment = readSubExpr(object, "increment");
        RuntimeStmt body = readStmt(GsonHelper.getAsJsonObject(object, "body"));
        return null;// new RuntimeStmt.For(init, condition, increment, body);
    }

    private static RuntimeStmt readLoopInterrupt(JsonObject object) {
        return new RuntimeStmt.LoopInterruption(TokenType.readFromSubObject(object, "keyword"));
    }

    public static RuntimeAnnotationClassInstance[] readAnnotations(JsonObject data) {
        List<RuntimeAnnotationClassInstance> list = new ArrayList<>();
        JsonArray annotationData = GsonHelper.getAsJsonArray(data, "annotations");
        for (JsonElement e : annotationData) {
            JsonObject d = (JsonObject) e;
            ScriptedClass clazz = ClassLoader.loadClassReference(d, "type").get();
            Map<String, RuntimeExpr> properties = new HashMap<>();
            GsonHelper.getAsJsonObject(d, "properties").asMap().forEach((string, jsonElement) -> properties.put(string, readExpr((JsonObject) jsonElement)));
            //list.add(new AnnotationClassInstance(clazz, properties));
        }
        return list.toArray(new RuntimeAnnotationClassInstance[0]);
    }
}
