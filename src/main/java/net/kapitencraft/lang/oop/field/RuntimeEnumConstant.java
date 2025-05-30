package net.kapitencraft.lang.oop.field;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.ast.RuntimeExpr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.DynamicClassInstance;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.tool.GsonHelper;

import java.util.Map;
import java.util.function.Function;

public class RuntimeEnumConstant implements ScriptedField {

    private final ScriptedClass target;
    private final int ordinal;
    private final String name;
    private final int constructorOrdinal;
    private final RuntimeExpr[] args;

    public RuntimeEnumConstant(ScriptedClass target, int ordinal, String name, int constructorOrdinal, RuntimeExpr[] args) {
        this.target = target;
        this.ordinal = ordinal;
        this.name = name;
        this.constructorOrdinal = constructorOrdinal;
        this.args = args;
    }


    @Override
    public Object initialize(Environment environment, Interpreter interpreter) {
        DynamicClassInstance constant = new DynamicClassInstance(this.target, interpreter);
        constant.assignField("ordinal", ordinal);
        constant.assignField("name", name);
        constant.construct(interpreter.visitArgs(this.args), constructorOrdinal, interpreter);
        return constant;
    }

    @Override
    public ClassReference type() {
        return null;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    public static RuntimeEnumConstant fromJson(JsonObject object, ScriptedClass target) {
        int ordinal = GsonHelper.getAsInt(object, "ordinal");
        String name = GsonHelper.getAsString(object, "name");
        int cOrdinal = GsonHelper.getAsInt(object, "constructorOrdinal");
        RuntimeExpr[] args = CacheLoader.readArgs(object, "args");
        return new RuntimeEnumConstant(target, ordinal, name, cOrdinal, args);
    }

    public JsonObject cache(CacheBuilder builder) {
        JsonObject object = new JsonObject();
        object.addProperty("ordinal", this.ordinal);
        object.addProperty("name", this.name);
        object.addProperty("constructorOrdinal", this.constructorOrdinal);
        //object.add("args", builder.saveArgs(this.args));
        return object;
    }

    public static Function<ScriptedClass, Map<String, RuntimeEnumConstant>> loadFieldMap(JsonObject data, String member) {
        return target -> {
            ImmutableMap.Builder<String, RuntimeEnumConstant> fields = new ImmutableMap.Builder<>();
            {
                JsonObject fieldData = GsonHelper.getAsJsonObject(data, member);
                fieldData.asMap().forEach((name1, element) ->
                        fields.put(name1, RuntimeEnumConstant.fromJson(element.getAsJsonObject(), target)));
            }
            return fields.build();
        };
    }

}
