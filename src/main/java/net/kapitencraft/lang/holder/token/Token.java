package net.kapitencraft.lang.holder.token;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.holder.LiteralHolder;
import net.kapitencraft.tool.GsonHelper;

public record Token(TokenType type, String lexeme, LiteralHolder literal, int line, int lineStartIndex) {

    public String toString() {
        return String.format("Token{type=%s, lexeme=\"%s\", literal=%s}@line%s", type, lexeme, literal, line);
    }

    public static Token readFromSubObject(JsonObject object, String name) {
        return fromJson(GsonHelper.getAsJsonObject(object, name));
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

    public Token after() {
        return new Token(this.type, this.lexeme, this.literal, this.line, this.lineStartIndex + this.lexeme.length());
    }
}
