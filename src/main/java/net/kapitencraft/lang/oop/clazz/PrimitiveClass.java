package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.oop.LoxField;

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
    public Object getStaticField(String name) {
        return null;
    }

    @Override
    public Object assignStaticField(String name, Object val) {
        return null;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "Primitive$" + name;
    }

    @Override
    public String packageRepresentation() {
        return "scripted.lang."; //TODO perhaps add package field?
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
    public LoxCallable getStaticMethod(String name, List<? extends LoxClass> args) {
        return null;
    }

    @Override
    public LoxCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return 0;
    }

    @Override
    public LoxCallable getMethod(String name, List<LoxClass> args) {
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
    public MethodContainer getConstructor() {
        return null;
    }

    @Override
    public Map<String, MethodContainer> getMethods() {
        return Map.of();
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
        return -1;
    }

    @Override
    public boolean hasEnclosing(String name) {
        return false;
    }

    @Override
    public LoxClass getEnclosing(String name) {
        return null;
    }

    public Object defaultValue() {
        return defaultValue;
    }
}
