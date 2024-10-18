package net.kapitencraft.lang.run;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.compile.Compiler;
import net.kapitencraft.lang.oop.clazz.LoxClass;

import java.util.List;
import java.util.Map;
import java.util.Random;

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

                @Override
                public boolean isAbstract() {
                    return false;
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

                @Override
                public boolean isAbstract() {
                    return false;
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

                @Override
                public boolean isAbstract() {
                    return false;
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

                @Override
                public boolean isAbstract() {
                    return false;
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

                @Override
                public boolean isAbstract() {
                    return false;
                }
            }
    );

    public static void error(int lineIndex, String message, String fileId, String line) {
        Compiler.report(lineIndex, message, fileId, line.length(), line);
    }
}