package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

public class StackOverflowExceptionClass extends ThrowableClass {
    public StackOverflowExceptionClass() {
        super("StackOverflowException", "scripted.lang");
    }

    @Override
    public @Nullable ClassReference superclass() {
        return VarTypeManager.THROWABLE;
    }
}
