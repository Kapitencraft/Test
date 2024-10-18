package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.oop.clazz.LoxClass;

public class MissingVarExceptionClass extends ThrowableClass {

    @Override
    public String name() {
        return "MissingVarException";
    }

    @Override
    public LoxClass superclass() {
        return VarTypeManager.THROWABLE;
    }
}
