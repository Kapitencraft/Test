package net.kapitencraft.lang.holder;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantDoubleInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantFloatInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantIntegerInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantPoolEntry;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.tool.GsonHelper;

public record LiteralHolder(Object value, ScriptedClass type) {

    public static final LiteralHolder EMPTY = new LiteralHolder(null, null);

    public ConstantPoolEntry toBytecode() {
        if (this == EMPTY)
            throw new IllegalAccessError("can not convert empty literal to bytecode");
        if (type == VarTypeManager.INTEGER) {
            return new ConstantIntegerInfo((Integer) value);
        } else if (type == VarTypeManager.FLOAT) {
            return new ConstantFloatInfo((Float) value);
        } else if (type == VarTypeManager.DOUBLE) {
            return new ConstantDoubleInfo((Double) value);
        } else if (type == VarTypeManager.BOOLEAN) {
            return new ConstantIntegerInfo((Boolean) value ? 1 : 0);
        } else
            throw new IllegalArgumentException("unknown literal holder type: " + type);
    }

    public static LiteralHolder fromJson(JsonObject object) {
        if (object.isEmpty()) return EMPTY;
        String type = GsonHelper.getAsString(object, "type");
        ScriptedClass target = switch (type) {
            case "int" -> VarTypeManager.INTEGER;
            case "float" -> VarTypeManager.FLOAT;
            case "double" -> VarTypeManager.DOUBLE;
            case "bool" -> VarTypeManager.BOOLEAN;
            case "char" -> VarTypeManager.CHAR;
            case "String" -> VarTypeManager.STRING.get();
            default -> throw new IllegalArgumentException("unknown primitive type");
        };
        Object val = null;
        if (object.has("value")) {
            if (target == VarTypeManager.INTEGER) {
                val = GsonHelper.getAsInt(object, "value");
            } else if (target == VarTypeManager.FLOAT) {
                val = GsonHelper.getAsFloat(object, "value");
            } else if (target == VarTypeManager.DOUBLE) {
                val = GsonHelper.getAsDouble(object, "value");
            } else if (target == VarTypeManager.BOOLEAN) {
                val = GsonHelper.getAsBoolean(object, "value");
            } else if (target == VarTypeManager.CHAR) {
                val = GsonHelper.getAsCharacter(object, "value");
            } else
                val = GsonHelper.getAsString(object, "value");
        }
        return new LiteralHolder(val, target);
    }
}
