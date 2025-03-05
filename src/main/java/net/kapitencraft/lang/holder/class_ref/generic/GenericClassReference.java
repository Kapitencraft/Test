package net.kapitencraft.lang.holder.class_ref.generic;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.VarTypeManager;

public class GenericClassReference extends ClassReference {
    private final ClassReference lowerBound, upperBound;
    private final String name;

    public GenericClassReference(String name, ClassReference lowerBound, ClassReference upperBound) {
        super(name, "");
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public String absoluteName() {
        return "?" + (lowerBound != null ? " extends " + lowerBound.absoluteName() : "") + (upperBound != null ? " super " + upperBound.absoluteName() : "");
    }

    @Override
    public String name() {
        return absoluteName();
    }

    @Override
    public boolean exists() {
        return lowerBound == null || lowerBound.exists() && upperBound == null || upperBound.exists();
    }

    @Override
    public ScriptedClass get(GenericStack generics) {
        return generics == null ?
                lowerBound == null ?
                        VarTypeManager.OBJECT.get() :
                        lowerBound.get() :
                generics.getValue(name).orElse(lowerBound == null ?
                        VarTypeManager.OBJECT :
                        lowerBound).get(generics);
    }
}
