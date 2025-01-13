package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.util.Objects;
import java.util.function.Supplier;

public class ClassReference implements Supplier<ScriptedClass> {
    private ScriptedClass target;
    private final String name;

    public ClassReference(String name) {
        this.name = name;
    }

    public String absoluteName() {
        return get().absoluteName();
    }

    public static ClassReference of(ScriptedClass setTarget) {
        ClassReference reference = new ClassReference(setTarget.name());
        reference.setTarget(setTarget);
        return reference;
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
        return new ClassWrapper(this.name + "[]", () -> target.array());
    }

    @Override
    public String toString() {
        return "ClassReference@" + this.name + (target != null ? ", applied: " + target : "");
    }
}
