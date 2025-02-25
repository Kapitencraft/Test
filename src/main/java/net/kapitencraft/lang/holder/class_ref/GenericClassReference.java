package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.VarTypeManager;

public class GenericClassReference extends ClassReference {
    private final ClassReference[] lowerBound, upperBound;

    public GenericClassReference(String name, ClassReference[] lowerBound, ClassReference[] upperBound) {
        super(name, "");
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public ScriptedClass get() {
        return lowerBound.length == 0 ? VarTypeManager.OBJECT.get() : lowerBound[0].get();
    }
}
