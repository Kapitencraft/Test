package net.kapitencraft.lang.tool;

import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.token.Token;

public class Math {
    public static Object specialMerge(Object in, Token type) {
        if (in instanceof Integer i) {
            return i + (type.type() == TokenType.GROW ? 1 : -1);
        } else if (in instanceof Float f) {
            return f + (type.type() == TokenType.GROW ? 1 : -1);
        } else {
            return (double)in + (type.type() == TokenType.GROW ? 1 : -1);
        }
    }
}
