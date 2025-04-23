package net.kapitencraft.lang.oop.field;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.ast.RuntimeExpr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.GsonHelper;

public class RuntimeField implements ScriptedField {
    private final ClassReference type;
    private final RuntimeExpr init;
    private final boolean isFinal;
    private final RuntimeAnnotationClassInstance[] annotations;

    public RuntimeField(ClassReference type, RuntimeExpr init, boolean isFinal, RuntimeAnnotationClassInstance[] annotations) {
        this.type = type;
        this.init = init;
        this.isFinal = isFinal;
        this.annotations = annotations;
    }

    @Override
    public Object initialize(Environment environment, Interpreter interpreter) {
        if (!hasInit()) {
            if (type.get() instanceof PrimitiveClass prim) {
                return prim.defaultValue();
            }
            return null;
        }
        return interpreter.evaluate(init);
    }

    public boolean hasInit() {
        return init != null;
    }

    @Override
    public ClassReference type() {
        return type;
    }

    public static RuntimeField fromJson(JsonObject object) {
        ClassReference type = ClassLoader.loadClassReference(object, "type");
        RuntimeExpr init = CacheLoader.readOptionalSubExpr(object, "init");
        boolean isFinal = object.has("isFinal") && GsonHelper.getAsBoolean(object, "isFinal");
        RuntimeAnnotationClassInstance[] annotations = CacheLoader.readAnnotations(object);
        return new RuntimeField(type, init, isFinal, annotations);
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
        return isFinal;
    }
}
