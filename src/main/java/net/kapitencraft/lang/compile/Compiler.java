package net.kapitencraft.lang.compile;

import net.kapitencraft.lang.ast.token.Token;
import net.kapitencraft.lang.ast.Stmt;

import java.util.List;

public class Compiler {
    static boolean hadError = false;

    public static List<Stmt> compile(String source, String[] lines) {
        LangScanner scanner = new LangScanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens, lines);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) System.exit(65);

        return statements;
    }

    public static void error(int lineIndex, String message, String line) {
        report(lineIndex, message, line.length(), line);
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
