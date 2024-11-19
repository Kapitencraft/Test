package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.oop.clazz.LoxClass;

public class StackOverflowExceptionClass extends ThrowableClass {
    public StackOverflowExceptionClass() {
        super("StackOverflowException", "scripted.lang", VarTypeManager.THROWABLE.get());
    }
}
