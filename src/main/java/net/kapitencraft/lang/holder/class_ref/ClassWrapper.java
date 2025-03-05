package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ClassWrapper extends ClassReference {
    private final UnaryOperator<ScriptedClass> target;
    private final ClassReference origin;

    public ClassWrapper(String name, String pck, UnaryOperator<ScriptedClass> target, ClassReference origin) {
        super(name, pck);
        this.target = target;
        this.origin = origin;
    }

    @Override
    public boolean exists() {
        return origin.exists();
    }

    @Override
    public ScriptedClass get(GenericStack generics) {
        return target.apply(origin.get(generics));
    }
}
