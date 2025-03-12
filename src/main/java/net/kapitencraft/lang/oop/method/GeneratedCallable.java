package net.kapitencraft.lang.oop.method;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.exception.CancelBlock;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class GeneratedCallable implements ScriptedCallable {
    private final ClassReference retType;
    private final List<? extends Pair<? extends ClassReference, String>> params;
    private final List<Stmt> body;
    private final short modifiers;
    private final AnnotationClassInstance[] annotations;

    public GeneratedCallable(ClassReference retType, List<? extends Pair<? extends ClassReference, String>> params, List<Stmt> body, short modifiers, AnnotationClassInstance[] annotations) {
        this.retType = retType;
        this.params = params;
        this.body = body;
        this.modifiers = modifiers;
        this.annotations = annotations;
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
        if (!Modifiers.isAbstract(modifiers)) {
            JsonArray array = new JsonArray();
            body.stream().map(builder::cache).forEach(array::add);
            object.add("body", array);
        }
        if (this.modifiers != 0) object.addProperty("modifiers", this.modifiers);

        object.add("annotations", builder.cacheAnnotations(this.annotations));
        return object;
    }

    public static GeneratedCallable load(JsonObject data) {
        ClassReference retType = ClassLoader.loadClassReference(data, "retType");
        JsonArray paramData = GsonHelper.getAsJsonArray(data, "params");

        List<Pair<ClassReference, String>> params = paramData.asList().stream().map(JsonElement::getAsJsonObject).map(object1 -> {
            ClassReference type = ClassLoader.loadClassReference(object1, "type");
            String argName = GsonHelper.getAsString(object1, "name");
            return Pair.of(type, argName);
        }).toList();

        short modifiers = data.has("modifiers") ? GsonHelper.getAsShort(data, "modifiers") : 0;

        List<Stmt> body;
        if (Modifiers.isAbstract(modifiers)) body = null;
        else body = CacheLoader.readStmtList(data, "body");

        AnnotationClassInstance[] annotations = CacheLoader.readAnnotations(data);

        return new GeneratedCallable(retType, params, body, modifiers, annotations);
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
    public boolean isFinal() {
        return Modifiers.isFinal(modifiers);
    }

    @Override
    public ClassReference type() {
        return retType;
    }

    @Override
    public ClassReference[] argTypes() {
        return params.stream().map(Pair::left).toArray(ClassReference[]::new);
    }
}