package net.kapitencraft.lang.exe.algebra;

import com.google.gson.JsonObject;
import net.kapitencraft.tool.GsonHelper;

public enum Operand {
    LEFT,
    RIGHT;

    public static Operand fromJson(JsonObject object, String name) {
        return Operand.valueOf(GsonHelper.getAsString(object, name));
    }
}
