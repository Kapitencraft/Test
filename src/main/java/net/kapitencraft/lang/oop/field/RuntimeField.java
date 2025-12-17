package net.kapitencraft.lang.oop.field;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;

public class RuntimeField implements ScriptedField {
    private final ClassReference type;
    private final short modifiers;

    public RuntimeField(ClassReference type, short modifiers) {
        this.type = type;
        this.modifiers = modifiers;
    }

    @Override
    public ClassReference type() {
        return type;
    }

    public static RuntimeField fromJson(JsonObject object) {
        ClassReference type = ClassLoader.loadClassReference(object, "type");
        short modifiers = GsonHelper.getAsShort(object, "modifiers");
        Annotation[] annotations = Annotation.readAnnotations(object);
        return new RuntimeField(type, modifiers);
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

    @Override
    public short modifiers() {
        return modifiers;
    }
}
