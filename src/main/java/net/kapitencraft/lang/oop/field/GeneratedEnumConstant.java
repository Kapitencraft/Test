package net.kapitencraft.lang.oop.field;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.tool.GsonHelper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GeneratedEnumConstant extends ScriptedField {

    private final LoxClass target;
    private final int ordinal;
    private final String name;
    private final int constructorOrdinal;
    private final List<Expr> args;

    public GeneratedEnumConstant(LoxClass target, int ordinal, String name, int constructorOrdinal, List<Expr> args) {
        this.target = target;
        this.ordinal = ordinal;
        this.name = name;
        this.constructorOrdinal = constructorOrdinal;
        this.args = args;
    }


    @Override
    public Object initialize(Environment environment, Interpreter interpreter) {
        ClassInstance constant = new ClassInstance(this.target, interpreter);
        constant.assignField("ordinal", ordinal);
        constant.assignField("name", name);
        constant.construct(interpreter.visitArgs(this.args), constructorOrdinal, interpreter);
        return constant;
    }

    @Override
    public ClassReference getType() {
        return null;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    public static GeneratedEnumConstant fromJson(JsonObject object, LoxClass target) {
        int ordinal = GsonHelper.getAsInt(object, "ordinal");
        String name = GsonHelper.getAsString(object, "name");
        int cOrdinal = GsonHelper.getAsInt(object, "constructorOrdinal");
        List<Expr> args = CacheLoader.readArgs(object, "args");
        return new GeneratedEnumConstant(target, ordinal, name, cOrdinal, args);
    }

    public JsonObject cache(CacheBuilder builder) {
        JsonObject object = new JsonObject();
        object.addProperty("ordinal", this.ordinal);
        object.addProperty("name", this.name);
        object.addProperty("constructorOrdinal", this.constructorOrdinal);
        object.add("args", builder.saveArgs(this.args));
        return object;
    }

    public static Function<LoxClass, Map<String, GeneratedEnumConstant>> loadFieldMap(JsonObject data, String member) {
        return target -> {
            ImmutableMap.Builder<String, GeneratedEnumConstant> fields = new ImmutableMap.Builder<>();
            {
                JsonObject fieldData = GsonHelper.getAsJsonObject(data, member);
                fieldData.asMap().forEach((name1, element) ->
                        fields.put(name1, GeneratedEnumConstant.fromJson(element.getAsJsonObject(), target)));
            }
            return fields.build();
        };
    }

}
