package net.kapitencraft.lang.compile;

import net.kapitencraft.lang.compile.visitor.LocationFinder;
import net.kapitencraft.lang.compile.visitor.Resolver;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.ast.Stmt;

import java.util.List;

public class Compiler {
    static boolean hadError = false;

    public static class ErrorLogger {
        private final String[] lines;
        private final LocationFinder finder;

        public ErrorLogger(String[] lines) {
            this.lines = lines;
            finder = new LocationFinder();
        }

        public void error(Token loc, String msg) {
            Compiler.error(loc, msg, lines[loc.line - 1]);
        }

        public void error(Stmt loc, String msg) {
            error(finder.find(loc), msg);
        }

        public void error(Expr loc, String msg) {
            error(finder.find(loc), msg);
        }
    }

    public static Stmt compile(String source, String[] lines) {
        ErrorLogger consumer = new ErrorLogger(lines);
        Lexer scanner = new Lexer(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens, consumer);
        Stmt.Class stmt = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) System.exit(65);

        Resolver resolver = new Resolver(consumer);
        System.out.println("Resolving...");
        resolver.resolve(stmt);

        if (hadError) System.exit(65);

        return stmt;
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
