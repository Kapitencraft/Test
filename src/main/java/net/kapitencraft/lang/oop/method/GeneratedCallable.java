package net.kapitencraft.lang.oop.method;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compile.CacheBuilder;
import net.kapitencraft.lang.exception.CancelBlock;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.CacheLoader;
import net.kapitencraft.lang.run.ClassLoader;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class GeneratedCallable implements LoxCallable {
    private final Stmt.FuncDecl declaration;

    public GeneratedCallable(Stmt.FuncDecl declaration) {
        this.declaration = declaration;
    }

    public JsonObject save(CacheBuilder builder) {
        JsonObject object = new JsonObject();
        object.addProperty("retType", declaration.retType.absoluteName());
        object.add("name", declaration.name.toJson());
        {
            JsonArray array = new JsonArray();
            declaration.params.forEach(pair -> {
                JsonObject object1 = new JsonObject();
                object1.addProperty("type", pair.left().absoluteName());
                object1.add("name", pair.right().toJson());
                array.add(object1);
            });
            object.add("params", array);
        }
        {
            JsonArray array = new JsonArray();
            declaration.body.stream().map(builder::cache).forEach(array::add);
            object.add("body", array);
        }
        {
            JsonArray array = new JsonArray();
            if (declaration.isFinal) array.add("isFinal");
            if (declaration.isAbstract) array.add("isAbstract");
            object.add("flags", array);
        }

        return object;
    }

    public static GeneratedCallable load(JsonObject object) {
        LoxClass retType = ClassLoader.loadClassReference(object, "retType");
        Token name = Token.readFromSubObject(object, "name");
        JsonArray paramData = GsonHelper.getAsJsonArray(object, "params");

        List<Pair<LoxClass, Token>> params = paramData.asList().stream().map(JsonElement::getAsJsonObject).map(object1 -> {
            LoxClass type = ClassLoader.loadClassReference(object1, "type");
            Token argName = Token.readFromSubObject(object1, "name");
            return Pair.of(type, argName);
        }).toList();

        List<String> flags = ClassLoader.readFlags(object);

        return new GeneratedCallable(new Stmt.FuncDecl(retType, name, params, CacheLoader.readStmtList(object, "body"), flags.contains("isFinal"), flags.contains("isAbstract")));
    }

    @Override
    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
        if (declaration.body == null) {
            throw new IllegalAccessError("abstract method called directly! this shouldn't happen...");
        }

        for (int i = 0; i < declaration.params.size(); i++) {
            environment.defineVar(declaration.params.get(i).right().lexeme(), arguments.get(i));
        }

        try {
            interpreter.interpret(declaration.body, environment);
        } catch (CancelBlock returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public boolean isAbstract() {
        return declaration.body == null;
    }

    @Override
    public LoxClass type() {
        return declaration.retType;
    }

    @Override
    public List<? extends LoxClass> argTypes() {
        return declaration.params.stream().map(Pair::left).toList();
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<declared fn#" + declaration.name.lexeme() + ">";
    }
}