package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

public class ArithmeticExceptionClass extends ThrowableClass {
    public ArithmeticExceptionClass() {
        super("ArithmeticException", "scripted.lang");
    }

    @Override
    public String name() {
        return "ArithmeticException";
    }

    @Override
    public @Nullable ClassReference superclass() {
        return VarTypeManager.THROWABLE;
    }
}
