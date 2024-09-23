package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.oop.LoxField;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;
import java.util.Map;

public class PrimitiveClass implements LoxClass {
    private final String name;
    private final LoxClass superclass;
    private final Object defaultValue;

    public PrimitiveClass(LoxClass superclass, String name, Object defaultValue) {
        this.name = name;
        this.superclass = superclass;
        this.defaultValue = defaultValue;
    }

    public PrimitiveClass(String name, Object defaultValue) {
        this(null, name, defaultValue);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public LoxClass superclass() {
        return superclass;
    }

    @Override
    public LoxClass getFieldType(String name) {
        return null;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return null;
    }

    @Override
    public boolean hasField(String name) {
        return false;
    }

    @Override
    public LoxClass getStaticMethodType(String name) {
        return null;
    }

    @Override
    public LoxClass getMethodType(String name) {
        return null;
    }

    @Override
    public LoxCallable getStaticMethod(String name) {
        return null;
    }

    @Override
    public LoxCallable getMethod(String name) {
        return null;
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return false;
    }

    @Override
    public boolean hasMethod(String name) {
        return false;
    }

    @Override
    public Map<String, LoxField> getFields() {
        return Map.of();
    }

    @Override
    public void callConstructor(Environment environment, Interpreter interpreter, List<Object> args) {

    }

    public Object defaultValue() {
        return defaultValue;
    }
}
