package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class PrimitiveClass implements LoxClass {
    private final String name;
    private final LoxClass superclass;
    private final Object defaultValue;

    public PrimitiveClass(LoxClass superclass, String name, Object defaultValue) {
        this.name = name;
        this.superclass = superclass;
        this.defaultValue = defaultValue;
        VarTypeManager.getOrCreatePackage("scripted.lang").addClass(name, this);
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
    public boolean hasInit() {
        return true;
    }

    @Override
    public void setInit() {

    }

    @Override
    public Map<String, ? extends ScriptedField> staticFields() {
        return Map.of();
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
        return "scripted.lang.";
    }

    @Override
    public @Nullable ClassReference superclass() {
        return superclass == null ? null : superclass.reference();
    }

    @Override
    public ClassReference getFieldType(String name) {
        return null;
    }

    @Override
    public ClassReference getStaticFieldType(String name) {
        return null;
    }

    @Override
    public boolean hasField(String name) {
        return false;
    }

    @Override
    public ScriptedCallable getStaticMethod(String name, List<ClassReference> args) {
        return null;
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<ClassReference> args) {
        return 0;
    }

    @Override
    public ScriptedCallable getMethod(String name, List<ClassReference> args) {
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
    public Map<String, ScriptedField> getFields() {
        return Map.of();
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
        return -1;
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

    public Object defaultValue() {
        return defaultValue;
    }

    @Override
    public ClassReference[] enclosed() {
        return new ClassReference[0];
    }

    @Override
    public ClassReference[] interfaces() {
        return new ClassReference[0];
    }
}
