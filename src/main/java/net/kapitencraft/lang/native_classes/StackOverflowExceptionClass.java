package net.kapitencraft.lang.native_classes;

import net.kapitencraft.lang.VarTypeManager;
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
