package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.run.VarTypeManager;

public class IndexOutOfBoundsException extends ThrowableClass {

    public IndexOutOfBoundsException() {
        super("IndexOutOfBoundsException", "scripted.lang", VarTypeManager.THROWABLE.get());
    }
}
