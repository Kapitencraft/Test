package net.kapitencraft.lang.func;

import net.kapitencraft.lang.oop.clazz.LoxClass;

import java.util.List;

public abstract class NativeMethod implements ScriptedCallable {
    private final List<? extends LoxClass> arguments;
    private final LoxClass retType;
    private final boolean isFinal;

    public NativeMethod(List<? extends LoxClass> arguments, LoxClass retType, boolean isFinal) {
        this.arguments = arguments;
        this.retType = retType;
        this.isFinal = isFinal;
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
        return false;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }
}
