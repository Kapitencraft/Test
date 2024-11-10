package net.kapitencraft.lang.func;

import net.kapitencraft.lang.oop.clazz.LoxClass;

import java.util.List;

public abstract class NativeMethodImpl implements ScriptedCallable {
    private final List<? extends LoxClass> arguments;
    private final LoxClass retType;
    private final boolean isFinal, isAbstract;

    public NativeMethodImpl(List<? extends LoxClass> arguments, LoxClass retType, boolean isFinal, boolean isAbstract) {
        this.arguments = arguments;
        this.retType = retType;
        this.isFinal = isFinal;
        this.isAbstract = isAbstract;
    }

    @Override
    public LoxClass type() {
        return retType;
    }

    @Override
    public List<? extends LoxClass> argTypes() {
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
