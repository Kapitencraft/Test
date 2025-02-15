package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.lang.oop.method.map.AnnotationMethodMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class AnnotationClass implements AbstractAnnotationClass {
    private final String name, pck;

    protected final AnnotationMethodMap methods;

    private final Map<String, ClassReference> enclosed;

    public AnnotationClass(String name, String pck, AnnotationMethodMap methods, Map<String, ClassReference> enclosed) {
        this.name = name;
        this.pck = pck;
        this.methods = methods;
        this.enclosed = enclosed;
    }

    @Override
    public boolean hasInit() {
        return true;
    }

    @Override
    public void setInit() {

    }

    @Override
    public ClassReference[] enclosed() {
        return enclosed.values().toArray(new ClassReference[0]);
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
    public String pck() {
        return pck;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return null;
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<ClassReference> args) {
        return -1;
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
        return true;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return methods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, List<ClassReference> types) {
        return methods.getMethodOrdinal(name, types);
    }

    @Override
    public boolean hasEnclosing(String name) {
        return enclosed.containsKey(name);
    }

    @Override
    public ClassReference getEnclosing(String name) {
        return enclosed.get(name);
    }

    @Override
    public AbstractMethodMap getMethods() {
        return methods;
    }

    @Override
    public List<String> getAbstracts() {
        return methods.getAbstracts();
    }
}
