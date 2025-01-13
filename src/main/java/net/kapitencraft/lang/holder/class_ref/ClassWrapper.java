package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.util.function.Supplier;

public class ClassWrapper extends ClassReference {
    private final Supplier<ScriptedClass> target;

    public ClassWrapper(String name, Supplier<ScriptedClass> target) {
        super(name);
        this.target = target;
    }

    @Override
    public ScriptedClass get() {
        return target.get();
    }
}
