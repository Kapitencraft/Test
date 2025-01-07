package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

public class FunctionCallErrorClass extends ThrowableClass {
    public FunctionCallErrorClass() {
        super("FunctionCallError", "scripted.lang");
    }

    @Override
    public @Nullable ClassReference superclass() {
        return VarTypeManager.THROWABLE;
    }
}
