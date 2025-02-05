package net.kapitencraft.lang.holder.token;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.holder.LiteralHolder;
import net.kapitencraft.tool.GsonHelper;

public record Token(TokenType type, String lexeme, LiteralHolder literal, int line, int lineStartIndex) {

    public String toString() {
        return String.format("Token{type=%s, lexeme=\"%s\", literal=%s}@line%s", type, lexeme, literal, line);
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", type.name());
        object.addProperty("lexeme", lexeme);
        object.add("literal", literal.toJson());
        object.addProperty("line", line);
        object.addProperty("lineStartIndex", lineStartIndex);
        return object;
    }

    public static Token readFromSubObject(JsonObject object, String subElement) {
        return fromJson(GsonHelper.getAsJsonObject(object, subElement));
    }

    public static Token fromJson(JsonObject object) {
        TokenType type = TokenType.valueOf(GsonHelper.getAsString(object, "type"));
        String lexeme = GsonHelper.getAsString(object, "lexeme");
        LiteralHolder literal = LiteralHolder.fromJson(GsonHelper.getAsJsonObject(object, "literal"));
        int line = GsonHelper.getAsInt(object, "line");
        int lineStartIndex = GsonHelper.getAsInt(object, "lineStartIndex");
        return new Token(type, lexeme, literal, line, lineStartIndex);
    }

    public static Token createNative(String lexeme) {
        return new Token(TokenType.IDENTIFIER, lexeme, LiteralHolder.EMPTY, -1, -1);
    }
}
