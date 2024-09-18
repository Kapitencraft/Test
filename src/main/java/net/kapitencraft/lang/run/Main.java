package net.kapitencraft.lang.run;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.compile.Compiler;
import net.kapitencraft.lang.oop.LoxClass;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static Map<String, LoxCallable> natives = Map.of(
            "clock", new LoxCallable() {
                @Override
                public int arity() {
                    return 0;
                }

                @Override
                public LoxClass type() {
                    return VarTypeManager.INTEGER;
                }

                @Override
                public List<LoxClass> argTypes() {
                    return List.of();
                }

                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    return (double) (System.currentTimeMillis() - Interpreter.millisAtStart);
                }

                @Override
                public String toString() {
                    return "<native fn#clock>";
                }
            },
            "print", new LoxCallable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public LoxClass type() {
                    return VarTypeManager.VOID;
                }

                @Override
                public List<LoxClass> argTypes() {
                    return List.of(VarTypeManager.OBJECT);
                }

                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    System.out.println(Interpreter.stringify(arguments.get(0)));
                    return null;
                }
            },
            "randInt", new LoxCallable() {
                @Override
                public int arity() {
                    return 2;
                }

                @Override
                public LoxClass type() {
                    return VarTypeManager.INTEGER;
                }

                @Override
                public List<LoxClass> argTypes() {
                    return List.of(VarTypeManager.INTEGER, VarTypeManager.INTEGER);
                }

                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    Random random = new Random();
                    int min = (int) arguments.get(0);
                    int max = (int) arguments.get(1);
                    return random.nextInt((max - min) + 1) + min;
                }
            },
            "abs", new LoxCallable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public LoxClass type() {
                    return VarTypeManager.INTEGER;
                }

                @Override
                public List<LoxClass> argTypes() {
                    return List.of(VarTypeManager.NUMBER);
                }

                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    Number num = (Number) arguments.get(0);
                    if (num instanceof Integer i) return java.lang.Math.abs(i);
                    else if (num instanceof Double d) return java.lang.Math.abs(d);
                    else return java.lang.Math.abs((float) num);
                }
            },
            "input", new LoxCallable() {
                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public LoxClass type() {
                    return VarTypeManager.STRING;
                }

                @Override
                public List<LoxClass> argTypes() {
                    return List.of(VarTypeManager.STRING);
                }

                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    System.out.print(Interpreter.stringify(arguments.get(0)));
                    return Interpreter.in.nextLine();
                }
            }
    );

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
        report(token.line, message, token.lineStartIndex, line);
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

        Stmt statements = Compiler.compile(source, lines);

        interpreter.interpret(List.of(statements));
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