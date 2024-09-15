package net.kapitencraft.lang.compile;

import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.compile.analyser.Resolver;

import java.util.List;

public class Compiler {
    static boolean hadError = false;

    @FunctionalInterface
    public interface ErrorConsumer {
        void error(Token loc, String msg, String line);
    }

    public static List<Stmt> compile(String source, String[] lines) {
        Lexer scanner = new Lexer(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens, lines, Compiler::error);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) System.exit(65);

        Resolver resolver = new Resolver(Compiler::error, lines);
        System.out.println("Resolving...");
        resolver.resolve(statements);

        if (hadError) System.exit(65);

        return statements;
    }

    public static void error(Token token, String message, String line) {
        report(token.line, message, token.lineStartIndex, line);
    }

    private static void report(int lineIndex, String message, int startIndex, String line) {
        System.err.println("Error in line " + lineIndex + ": " + message);
        System.err.println(line);
        for (int i = 0; i < startIndex; i++) {
            System.err.print(" ");
        }
        System.err.println("^");

        hadError = true;
    }
}
