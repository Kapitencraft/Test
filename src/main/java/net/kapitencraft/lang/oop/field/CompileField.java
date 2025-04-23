package net.kapitencraft.lang.oop.field;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;
import net.kapitencraft.lang.run.Interpreter;

public class CompileField implements ScriptedField {
    private final ClassReference type;
    private final CompileExpr init;
    private final boolean isFinal;
    private final CompileAnnotationClassInstance[] annotations;

    public CompileField(ClassReference type, CompileExpr init, boolean isFinal, CompileAnnotationClassInstance[] annotations) {
        this.type = type;
        this.init = init;
        this.isFinal = isFinal;
        this.annotations = annotations;
    }

    @Override
    public Object initialize(Environment environment, Interpreter interpreter) {
        throw new IllegalAccessError("can not initialize Compile Field!");
    }

    public boolean hasInit() {
        return init != null;
    }

    @Override
    public ClassReference type() {
        return type;
    }

    public JsonElement cache(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type.absoluteName());
        if (hasInit()) object.add("init", cacheBuilder.cache(init));
        if (isFinal) object.addProperty("isFinal", true);
        object.add("annotations", cacheBuilder.cacheAnnotations(this.annotations));
        return object;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }
}
