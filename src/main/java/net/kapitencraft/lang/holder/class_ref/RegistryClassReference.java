package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.util.function.Supplier;

public class RegistryClassReference extends ClassReference {
    private final Supplier<ScriptedClass> sup;

    public RegistryClassReference(String name, Supplier<ScriptedClass> sup) {
        super(name);
        this.sup = sup;
    }

    public void create() {
        ScriptedClass val = sup.get(); //bruh
        this.setTarget(val);
    }
}
