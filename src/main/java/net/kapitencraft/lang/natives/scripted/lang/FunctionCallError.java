package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.oop.clazz.LoxClass;

public class FunctionCallError extends ThrowableClass {
    @Override
    public String name() {
        return "FunctionError";
    }

    @Override
    public LoxClass superclass() {
        return VarTypeManager.THROWABLE;
    }
}
