package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.func.NativeMethod;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.List;
import java.util.Map;

public class ObjectClass implements LoxClass {
    private final Map<String, DataMethodContainer> methods = Map.of(
            "toString", DataMethodContainer.of(new NativeMethod(List.of(), VarTypeManager.STRING) {
                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    return name();
                }
            }),
            "equals", DataMethodContainer.of(new NativeMethod(List.of(this), VarTypeManager.BOOLEAN) {
                @Override
                public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                    return environment.getThis() == arguments.get(0);
                }
            })
    );

    @Override
    public Object getStaticField(String name) {
        return null;
    }

    @Override
    public Object assignStaticField(String name, Object val) {
        return null;
    }

    @Override
    public LoxClass getFieldType(String name) {
        return VarTypeManager.VOID;
    }

    @Override
    public Map<String, LoxField> getFields() {
        return Map.of();
    }

    @Override
    public String name() {
        return "Object";
    }

    @Override
    public String packageRepresentation() {
        return "scripted.lang";
    }

    @Override
    public String absoluteName() {
        return "scripted.lang.Object";
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
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return 0;
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return false;
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
        return false;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return methods.get(name).getMethodByOrdinal(ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        return methods.get(name).getMethodOrdinal(types);
    }

    @Override
    public boolean hasEnclosing(String name) {
        return false;
    }

    @Override
    public LoxClass getEnclosing(String name) {
        return null;
    }

    @Override
    public Map<String, ? extends MethodContainer> getDeclaredMethods() {
        return methods;
    }
}
