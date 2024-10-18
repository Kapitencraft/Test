package net.kapitencraft.lang.natives.scripted.util;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.func.NativeMethod;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SystemClass implements LoxClass {
    private final Map<String, DataMethodContainer> staticMethods = setupMethods();


    private Map<String, DataMethodContainer> setupMethods() {
        ImmutableMap.Builder<String, DataMethodContainer> builder = new ImmutableMap.Builder<>();
        builder.put("print", new DataMethodContainer(new LoxCallable[]{
                new NativeMethod(List.of(VarTypeManager.OBJECT), VarTypeManager.VOID) {
                    @Override
                    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                        System.out.println(Interpreter.stringify(arguments.get(0)));
                        return null;
                    }
                }
        }));
        builder.put("time", new DataMethodContainer(new LoxCallable[]{
                new NativeMethod(List.of(), VarTypeManager.INTEGER) {
                    @Override
                    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                        return (int) (System.currentTimeMillis() - Interpreter.millisAtStart);
                    }
                }
        }));
        builder.put("input", new DataMethodContainer(new LoxCallable[]{
                new NativeMethod(List.of(VarTypeManager.OBJECT), VarTypeManager.STRING) {
                    @Override
                    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                        System.out.print(Interpreter.stringify(arguments.get(0)));
                        return Interpreter.in.nextLine();
                    }
                }
        }));
        return builder.build();
    }

    @Override
    public Object getStaticField(String name) {
        return null;
    }

    @Override
    public Object assignStaticField(String name, Object val) {
        return null;
    }

    @Override
    public String absoluteName() {
        return packageRepresentation() + "." + name();
    }

    @Override
    public String name() {
        return "System";
    }

    @Override
    public String packageRepresentation() {
        return "scripted.lang";
    }

    @Override
    public LoxClass superclass() {
        return null;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return null;
    }

    @Override
    public LoxCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return staticMethods.get(name).getMethodByOrdinal(ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return 0; //there's only one impl
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.containsKey(name);
    }

    @Override
    public MethodContainer getConstructor() {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return true;
    }

    @Override
    public LoxCallable getMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        return 0;
    }

    @Override
    public boolean hasEnclosing(String name) {
        return false;
    }

    @Override
    public LoxClass getEnclosing(String name) {
        return null;
    }
}
