package net.kapitencraft.lang.bytecode.compile;

import net.kapitencraft.lang.compiler.Lexer;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;

public class Compiler {
    private static final Lexer lexer = new Lexer(null);

    public static void compile(String source) {
        lexer.setSource(source);
        int line = -1;
        for (Token token = lexer.scanToken(); token.type() != TokenType.EOF; token = lexer.scanToken()) {
            if (token.line() != line) {
                System.out.printf("%4d ", token.line());
                line = token.line();
            } else {
                System.out.print("   | ");
            }
            System.out.printf("%2d '%s'\n", token.type().ordinal(), token.lexeme());
        }
    }
}
