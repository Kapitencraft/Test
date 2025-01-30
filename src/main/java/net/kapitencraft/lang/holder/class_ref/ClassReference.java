package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.util.Objects;
import java.util.function.Supplier;

public class ClassReference implements Supplier<ScriptedClass> {
    protected ScriptedClass target;
    private final String name;
    private final String pck;
    protected final ClassType type;

    public ClassReference(String name, String pck, ClassType type) {
        this.name = name;
        this.pck = pck;
        this.type = type;
    }

    public String absoluteName() {
        return pck + "." + name;
    }

    public String pck() {
        return pck;
    }

    public static ClassReference of(ScriptedClass target) {
        ClassReference reference = new ClassReference(target.name(), target.pck(), target.getClassType());
        reference.setTarget(target);
        return reference;
    }

    public ClassType getType() {
        return type;
    }

    public String name() {
        return name;
    }

    public void setTarget(ScriptedClass target) {
        this.target = target;
    }

    public ScriptedClass get() {
        return Objects.requireNonNull(target, "ScriptedClass not present: " + this.name);
    }

    public boolean is(ClassReference other) {
        return this.get().is(other.get());
    }

    public boolean is(ScriptedClass scriptedClass) {
        return this.get().is(scriptedClass);
    }

    public ClassReference array() {
        return new ClassWrapper(this.name + "[]", this.pck, () -> target.array());
    }

    @Override
    public String toString() {
        return "ClassReference@" + this.name + (target != null ? ", applied: " + target : "");
    }
}
