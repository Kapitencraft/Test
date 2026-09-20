package net.kapitencraft.lang.compiler;

import net.kapitencraft.lang.holder.token.Token;

import java.util.List;

public interface Lexer {
    List<Token> scanTokens();
}