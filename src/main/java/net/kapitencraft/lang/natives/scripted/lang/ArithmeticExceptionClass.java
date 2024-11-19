package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.oop.clazz.LoxClass;

public class ArithmeticExceptionClass extends ThrowableClass {
    public ArithmeticExceptionClass() {
        super("ArithmeticException", "scripted.lang", VarTypeManager.THROWABLE.get());
    }

    @Override
    public String name() {
        return "ArithmeticException";
    }

    @Override
    public LoxClass superclass() {
        return VarTypeManager.THROWABLE.get();
    }
}
