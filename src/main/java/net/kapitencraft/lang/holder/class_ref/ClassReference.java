package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.oop.clazz.LoxClass;

import java.util.Objects;
import java.util.function.Supplier;

public class ClassReference implements Supplier<LoxClass> {
    private LoxClass target;
    private final String name;

    public ClassReference(String name) {
        this.name = name;
    }

    public String absoluteName() {
        return get().absoluteName();
    }

    public static ClassReference of(LoxClass setTarget) {
        ClassReference reference = new ClassReference(setTarget.name());
        reference.setTarget(setTarget);
        return reference;
    }

    public String name() {
        return name;
    }

    public void setTarget(LoxClass target) {
        this.target = target;
    }

    public LoxClass get() {
        return Objects.requireNonNull(target, "ScriptedClass not present: " + this.name);
    }

    public boolean is(ClassReference other) {
        return this.get().is(other.get());
    }

    public boolean is(LoxClass loxClass) {
        return this.get().is(loxClass);
    }

    public ClassReference array() {
        return new ClassWrapper(this.name + "[]", () -> target.array());
    }

    @Override
    public String toString() {
        return "ClassReference@" + this.name + (target != null ? ", applied: " + target : "");
    }
}
