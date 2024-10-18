package net.kapitencraft.lang.func;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;

public abstract class NativeMethod implements LoxCallable {
    private final List<? extends LoxClass> arguments;
    private final LoxClass retType;

    public NativeMethod(List<? extends LoxClass> arguments, LoxClass retType) {
        this.arguments = arguments;
        this.retType = retType;
    }

    @Override
    public int arity() {
        return arguments.size();
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
}
