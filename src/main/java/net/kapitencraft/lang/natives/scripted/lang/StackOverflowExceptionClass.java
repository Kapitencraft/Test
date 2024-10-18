package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.oop.clazz.LoxClass;

public class StackOverflowExceptionClass extends ThrowableClass {
    @Override
    public LoxClass superclass() {
        return VarTypeManager.THROWABLE;
    }

    @Override
    public String name() {
        return "StackOverflowException";
    }
}
