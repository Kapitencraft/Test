package net.kapitencraft.tool;

import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.token.Token;

public class Math {
    public static Object specialMerge(Object in, Token type) {
        if (in instanceof Integer) {
            return (int)in + (type.type() == TokenType.GROW ? 1 : -1);
        } else if (in instanceof Float) {
            return (float) in + (type.type() == TokenType.GROW ? 1 : -1);
        } else {
            return (double)in + (type.type() == TokenType.GROW ? 1 : -1);
        }
    }
}
