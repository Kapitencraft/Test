package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.oop.clazz.LoxClass;

import java.util.function.Supplier;

public class ClassWrapper extends ClassReference {
    private final Supplier<LoxClass> target;

    public ClassWrapper(String name, Supplier<LoxClass> target) {
        super(name);
        this.target = target;
    }

    @Override
    public LoxClass get() {
        return target.get();
    }
}
