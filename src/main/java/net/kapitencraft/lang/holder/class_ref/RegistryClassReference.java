package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.util.function.Supplier;

public class RegistryClassReference extends ClassReference {
    private final Supplier<ScriptedClass> sup;

    public RegistryClassReference(String name, String pck, ClassType type, Supplier<ScriptedClass> sup) {
        super(name, pck, type);
        this.sup = sup;
    }

    public void create() {
        ScriptedClass val = sup.get(); //bruh
        this.setTarget(val);
    }
}
