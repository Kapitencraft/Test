package net.kapitencraft.lang.run;

import net.kapitencraft.lang.ast.Stmt;
import net.kapitencraft.lang.ast.token.Token;
import net.kapitencraft.lang.ast.token.TokenType;
import net.kapitencraft.lang.compile.Compiler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        runFile(args[0]);
        if (hadError) System.exit(65);

        if (hadRuntimeError) System.exit(70);
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    public static void error(Token token, String message, String line) {
        if (token.type == TokenType.EOF) {
            report(token.line, message, token.lineStartIndex, line);
        } else {
            report(token.line, message, token.lineStartIndex, line);
        }
    }

    private static void runPrompt() {
        Scanner scanner = new Scanner(System.in);

        for (;;) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line == null) break;
            run(line);
        }
    }

    private static void run(String source) {
        String[] lines = source.split("\n", Integer.MAX_VALUE);

        List<Stmt> statements = Compiler.compile(source, lines);

        interpreter.interpret(statements);
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
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