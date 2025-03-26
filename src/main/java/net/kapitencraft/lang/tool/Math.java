package net.kapitencraft.lang.tool;

import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.token.Token;

public class Math {
    public static Object specialMerge(Object in, TokenType type) {
        if (in instanceof Integer i) {
            return i + (type == TokenType.GROW ? 1 : -1);
        } else if (in instanceof Float f) {
            return f + (type == TokenType.GROW ? 1 : -1);
        } else {
            return (double)in + (type == TokenType.GROW ? 1 : -1);
        }
    }
}
