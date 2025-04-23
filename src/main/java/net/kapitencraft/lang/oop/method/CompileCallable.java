package net.kapitencraft.lang.oop.method;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.ast.CompileStmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.Pair;

import java.util.Arrays;
import java.util.List;

public class CompileCallable implements ScriptedCallable {
    private final ClassReference retType;
    private final List<? extends Pair<? extends ClassReference, String>> params;
    private final CompileStmt[] body;
    private final short modifiers;
    private final CompileAnnotationClassInstance[] annotations;

    public CompileCallable(ClassReference retType, List<? extends Pair<? extends ClassReference, String>> params, CompileStmt[] body, short modifiers, CompileAnnotationClassInstance[] annotations) {
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
            Arrays.stream(body).map(builder::cache).forEach(array::add);
            object.add("body", array);
        }
        if (this.modifiers != 0) object.addProperty("modifiers", this.modifiers);

        object.add("annotations", builder.cacheAnnotations(this.annotations));
        return object;
    }

    @Override
    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
        throw new IllegalAccessError("can not run Compile Callable!");
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