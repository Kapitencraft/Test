package net.kapitencraft.lang.oop.clazz.wrapper;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.field.NativeField;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class NativeUtilClass implements ScriptedClass {
    private final GeneratedMethodMap staticMethods;
    private final Map<String, NativeField> staticFields;
    private final String name;
    private final String pck;

    public NativeUtilClass(Map<String, DataMethodContainer> staticMethods, Map<String, NativeField> staticFields, String name, String pck) {
        this.staticMethods = new GeneratedMethodMap(staticMethods);
        this.staticFields = staticFields;
        this.name = name;
        this.pck = pck;
    }

    private boolean init = false;

    @Override
    public boolean hasInit() {
        return init;
    }

    @Override
    public void setInit() {
        init = true;
    }

    @Override
    public ClassReference[] enclosed() {
        return new ClassReference[0];
    }

    @Override
    public Map<String, ? extends ScriptedField> staticFields() {
        return staticFields;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String pck() {
        return pck;
    }

    @Override
    public String absoluteName() {
        return pck() + "." + name();
    }

    @Override
    public @Nullable ClassReference superclass() {
        return VarTypeManager.OBJECT;
    }

    @Override
    public ClassReference getStaticFieldType(String name) {
        return staticFields.get(name).getType();
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return staticMethods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<ClassReference> args) {
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
    public int getMethodOrdinal(String name, List<ClassReference> types) {
        return 0;
    }

    @Override
    public boolean hasEnclosing(String name) {
        return false;
    }

    @Override
    public ClassReference getEnclosing(String name) {
        return null;
    }

    @Override
    public GeneratedMethodMap getMethods() {
        return null;
    }

    @Override
    public ClassType getClassType() {
        return ClassType.CLASS;
    }
}
