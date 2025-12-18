package net.kapitencraft.lang.oop.field;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.VarTypeManager;

public class CompileField implements ScriptedField {
    private final ClassReference type;
    private final short modifiers;
    private final Annotation[] annotations;

    public CompileField(ClassReference type, short modifiers, Annotation[] annotations) {
        this.type = type;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    @Override
    public ClassReference type() {
        return type;
    }

    public JsonElement cache(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("type", VarTypeManager.getClassName(type));
        //if (hasInit()) object.add("init", cacheBuilder.cache(init)); TODO
        object.addProperty("modifiers", this.modifiers);
        object.add("annotations", cacheBuilder.cacheAnnotations(this.annotations));
        return object;
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(this.modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(this.modifiers);
    }

    @Override
    public short modifiers() {
        return modifiers;
    }
}
