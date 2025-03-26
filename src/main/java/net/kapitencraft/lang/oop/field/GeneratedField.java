package net.kapitencraft.lang.oop.field;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.GsonHelper;

public class GeneratedField extends ScriptedField {
    private final ClassReference type;
    private final Expr init;
    private final boolean isFinal;
    private final AnnotationClassInstance[] annotations;

    public GeneratedField(ClassReference type, Expr init, boolean isFinal, AnnotationClassInstance[] annotations) {
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
    public ClassReference getType() {
        return type;
    }

    public JsonElement cache(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type.absoluteName());
        //if (hasInit()) object.add("init", cacheBuilder.cache(init)); TODO
        if (isFinal) object.addProperty("isFinal", true);
        object.add("annotations", cacheBuilder.cacheAnnotations(this.annotations));
        return object;
    }

    public static GeneratedField fromJson(JsonObject object) {
        ClassReference type = ClassLoader.loadClassReference(object, "type");
        Expr init = CacheLoader.readOptionalSubExpr(object, "init");
        boolean isFinal = object.has("isFinal") && GsonHelper.getAsBoolean(object, "isFinal");
        AnnotationClassInstance[] annotations = CacheLoader.readAnnotations(object);
        return new GeneratedField(type, init, isFinal, annotations);
    }

    public static ImmutableMap<String, GeneratedField> loadFieldMap(JsonObject data, String member) {
        ImmutableMap.Builder<String, GeneratedField> fields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(data, member);
            fieldData.asMap().forEach((name1, element) ->
                    fields.put(name1, GeneratedField.fromJson(element.getAsJsonObject())));
        }
        return fields.build();
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }
}
