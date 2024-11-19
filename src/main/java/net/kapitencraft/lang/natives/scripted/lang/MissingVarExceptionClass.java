package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.oop.clazz.LoxClass;

public class MissingVarExceptionClass extends ThrowableClass {

    public MissingVarExceptionClass() {
        super("MissingVarException", "scripted.lang", VarTypeManager.THROWABLE.get());
    }
}
