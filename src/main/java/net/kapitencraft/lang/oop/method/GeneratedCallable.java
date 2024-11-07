package net.kapitencraft.lang.oop.method;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.exception.CancelBlock;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class GeneratedCallable implements ScriptedCallable {
    private final LoxClass retType;
    private final List<Pair<LoxClass,String>> params;
    private final List<Stmt> body;
    private final boolean isFinal, isAbstract;

    public GeneratedCallable(LoxClass retType, List<Pair<LoxClass, String>> params, List<Stmt> body, boolean isFinal, boolean isAbstract) {
        this.retType = retType;
        this.params = params;
        this.body = body;
        this.isFinal = isFinal;
        this.isAbstract = isAbstract;
    }

    public JsonObject save(CacheBuilder builder) {
        JsonObject object = new JsonObject();
        object.addProperty("retType", retType.absoluteName());
        {
            JsonArray array = new JsonArray();
            params.forEach(pair -> {
                JsonObject object1 = new JsonObject();
                object1.addProperty("type", pair.left().absoluteName());
                object1.addProperty("name", pair.right());
                array.add(object1);
            });
            object.add("params", array);
        }
        if (!isAbstract) {
            JsonArray array = new JsonArray();
            body.stream().map(builder::cache).forEach(array::add);
            object.add("body", array);
        }
        {
            JsonArray array = new JsonArray();
            if (isFinal) array.add("isFinal");
            if (isAbstract) array.add("isAbstract");
            object.add("flags", array);
        }

        return object;
    }

    public static GeneratedCallable load(JsonObject object) {
        LoxClass retType = ClassLoader.loadClassReference(object, "retType");
        JsonArray paramData = GsonHelper.getAsJsonArray(object, "params");

        List<Pair<LoxClass, String>> params = paramData.asList().stream().map(JsonElement::getAsJsonObject).map(object1 -> {
            LoxClass type = ClassLoader.loadClassReference(object1, "type");
            String argName = GsonHelper.getAsString(object1, "name");
            return Pair.of(type, argName);
        }).toList();

        List<String> flags = ClassLoader.readFlags(object);

        List<Stmt> body;
        if (flags.contains("isAbstract")) body = null;
        else body = CacheLoader.readStmtList(object, "body");

        return new GeneratedCallable(retType, params, body, flags.contains("isFinal"), flags.contains("isAbstract"));
    }

    @Override
    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
        if (body == null) {
            throw new IllegalAccessError("abstract method called directly! this shouldn't happen...");
        }

        for (int i = 0; i < params.size(); i++) {
            environment.defineVar(params.get(i).right(), arguments.get(i));
        }

        try {
            interpreter.interpret(body, environment);
        } catch (CancelBlock returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public boolean isAbstract() {
        return body == null;
    }

    @Override
    public LoxClass type() {
        return retType;
    }

    @Override
    public List<? extends LoxClass> argTypes() {
        return params.stream().map(Pair::left).toList();
    }
}