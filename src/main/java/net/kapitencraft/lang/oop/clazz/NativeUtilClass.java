package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.field.NativeField;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.List;
import java.util.Map;

public class NativeUtilClass implements LoxClass {
    private final MethodMap staticMethods;
    private final Map<String, NativeField> staticFields;
    private final String name;
    private final String pck;

    public NativeUtilClass(Map<String, DataMethodContainer> staticMethods, Map<String, NativeField> staticFields, String name, String pck) {
        this.staticMethods = new MethodMap(staticMethods);
        this.staticFields = staticFields;
        this.name = name;
        this.pck = pck;
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
        return staticFields.get(name).getType();
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return staticMethods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return staticMethods.getMethodOrdinal(name, args);
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.has(name);
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
    public boolean isInterface() {
        return false;
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
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

    @Override
    public MethodMap getMethods() {
        return null;
    }
}
