package net.kapitencraft.lang.oop.method;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.exception.CancelBlock;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.ast.RuntimeStmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class RuntimeCallable implements ScriptedCallable {
    private final ClassReference retType;
    private final List<? extends Pair<? extends ClassReference, String>> params;
    private final RuntimeStmt[] body;
    private final short modifiers;
    private final RuntimeAnnotationClassInstance[] annotations;

    public RuntimeCallable(ClassReference retType, List<? extends Pair<? extends ClassReference, String>> params, RuntimeStmt[] body, short modifiers, RuntimeAnnotationClassInstance[] annotations) {
        this.retType = retType;
        this.params = params;
        this.body = body;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    public static RuntimeCallable load(JsonObject data) {
        ClassReference retType = ClassLoader.loadClassReference(data, "retType");
        JsonArray paramData = GsonHelper.getAsJsonArray(data, "params");

        List<Pair<ClassReference, String>> params = paramData.asList().stream().map(JsonElement::getAsJsonObject).map(object1 -> {
            ClassReference type = ClassLoader.loadClassReference(object1, "type");
            String argName = GsonHelper.getAsString(object1, "name");
            return Pair.of(type, argName);
        }).toList();

        short modifiers = data.has("modifiers") ? GsonHelper.getAsShort(data, "modifiers") : 0;

        RuntimeStmt[] body;
        if (Modifiers.isAbstract(modifiers)) body = new RuntimeStmt[0];
        else body = CacheLoader.readStmtList(data, "body");

        RuntimeAnnotationClassInstance[] annotations = CacheLoader.readAnnotations(data);

        return new RuntimeCallable(retType, params, body, modifiers, annotations);
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