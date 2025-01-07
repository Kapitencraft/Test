package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.oop.clazz.LoxClass;

import java.util.function.Supplier;

public class RegistryClassReference extends ClassReference {
    private final Supplier<LoxClass> sup;

    public RegistryClassReference(String name, Supplier<LoxClass> sup) {
        super(name);
        this.sup = sup;
    }

    public void create() {
        LoxClass val = sup.get(); //bruh
        this.setTarget(val);
    }
}
