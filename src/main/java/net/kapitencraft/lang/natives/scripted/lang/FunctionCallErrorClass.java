package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.oop.clazz.LoxClass;

public class FunctionCallErrorClass extends ThrowableClass {
    public FunctionCallErrorClass() {
        super("FunctionCallError", "scripted.lang", VarTypeManager.THROWABLE.get());
    }
}
