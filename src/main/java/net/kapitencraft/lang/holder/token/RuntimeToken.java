package net.kapitencraft.lang.holder.token;

public record RuntimeToken(String lexeme, int line) {
    public static RuntimeToken createNative(String value) {
        return new RuntimeToken(value, -1);
    }
}
