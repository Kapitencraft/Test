package net.kapitencraft.lang.holder;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.tool.GsonHelper;

public record LiteralHolder(Object value, LoxClass type) {
    public static final LiteralHolder EMPTY = new LiteralHolder(null, null);

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        if (type == VarTypeManager.INTEGER) {
            object.addProperty("type", "int");
            object.addProperty("value", (int)value);
        } else if (type == VarTypeManager.FLOAT) {
            object.addProperty("type", "float");
            object.addProperty("value", (float)value);
        } else if (type == VarTypeManager.DOUBLE) {
            object.addProperty("type", "double");
            object.addProperty("value", (double)value);
        } else if (type == VarTypeManager.BOOLEAN) {
            object.addProperty("type", "bool");
            object.addProperty("value", (boolean)value);
        } else if (type == VarTypeManager.CHAR) {
            object.addProperty("type", "char");
            object.addProperty("value", (char)value);
        } else if (type == VarTypeManager.STRING.get()) {
            object.addProperty("type", "String");
            object.addProperty("value", (String) value);
        }
        return object;
    }

    public static LiteralHolder fromJson(JsonObject object) {
        if (object.isEmpty()) return EMPTY;
        String type = GsonHelper.getAsString(object, "type");
        LoxClass target = switch (type) {
            case "int" -> VarTypeManager.INTEGER;
            case "float" -> VarTypeManager.FLOAT;
            case "double" -> VarTypeManager.DOUBLE;
            case "bool" -> VarTypeManager.BOOLEAN;
            case "char" -> VarTypeManager.CHAR;
            case "String" -> VarTypeManager.STRING.get();
            default -> throw new IllegalArgumentException("unknown primitive type");
        };
        Object val;
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
        return new LiteralHolder(val, target);
    }
}
