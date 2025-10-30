package net.kapitencraft.lang.oop.field;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;

public class RuntimeField implements ScriptedField {
    private final ClassReference type;
    private final short modifiers;
    private final RuntimeAnnotationClassInstance[] annotations;

    public RuntimeField(ClassReference type, short modifiers, RuntimeAnnotationClassInstance[] annotations) {
        this.type = type;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    @Override
    public ClassReference type() {
        return type;
    }

    public static RuntimeField fromJson(JsonObject object) {
        ClassReference type = ClassLoader.loadClassReference(object, "type");
        short modifiers = GsonHelper.getAsShort(object, "modifiers");
        RuntimeAnnotationClassInstance[] annotations = CacheLoader.readAnnotations(object);
        return new RuntimeField(type, modifiers, annotations);
    }

    public static ImmutableMap<String, RuntimeField> loadFieldMap(JsonObject data, String member) {
        ImmutableMap.Builder<String, RuntimeField> fields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(data, member);
            fieldData.asMap().forEach((name1, element) ->
                    fields.put(name1, RuntimeField.fromJson(element.getAsJsonObject())));
        }
        return fields.build();
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(this.modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(this.modifiers);
    }
}
