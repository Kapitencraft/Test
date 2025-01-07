package net.kapitencraft.lang.run.natives.impl;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.LoxClass;

import java.util.List;

public abstract class NativeMethodImpl implements ScriptedCallable {
    private final List<ClassReference> arguments;
    private final ClassReference retType;
    private final boolean isFinal, isAbstract;

    public NativeMethodImpl(List<ClassReference> arguments, ClassReference retType, boolean isFinal, boolean isAbstract) {
        this.arguments = arguments;
        this.retType = retType;
        this.isFinal = isFinal;
        this.isAbstract = isAbstract;
    }

    @Override
    public ClassReference type() {
        return retType;
    }

    @Override
    public List<ClassReference> argTypes() {
        return arguments;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }
}
