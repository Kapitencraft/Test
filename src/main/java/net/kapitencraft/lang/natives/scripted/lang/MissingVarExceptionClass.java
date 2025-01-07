package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

public class MissingVarExceptionClass extends ThrowableClass {

    public MissingVarExceptionClass() {
        super("MissingVarException", "scripted.lang");
    }

    @Override
    public @Nullable ClassReference superclass() {
        return VarTypeManager.THROWABLE;
    }
}
