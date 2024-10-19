package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.List;
import java.util.Map;

public class NativeUtilClass implements LoxClass {
    private final Map<String, DataMethodContainer> staticMethods;
    private final String name;
    private final String pck;

    public NativeUtilClass(Map<String, DataMethodContainer> staticMethods, String name, String pck) {
        this.staticMethods = staticMethods;
        this.name = name;
        this.pck = pck;
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
    public String packageRepresentation() {
        return pck;
    }

    @Override
    public String absoluteName() {
        return packageRepresentation() + "." + name();
    }

    @Override
    public LoxClass superclass() {
        return VarTypeManager.OBJECT;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return null;
    }

    @Override
    public LoxCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return staticMethods.get(name).getMethodOrdinal(args);
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
        return false;
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
