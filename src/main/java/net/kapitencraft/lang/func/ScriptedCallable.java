package net.kapitencraft.lang.func;

import net.kapitencraft.lang.bytecode.exe.Chunk;
import net.kapitencraft.lang.holder.class_ref.ClassReference;

public interface ScriptedCallable {

    ClassReference type();

    ClassReference[] argTypes();

    Object call(Object[] arguments);

    default Chunk getChunk() {
        return null;
    }

    boolean isAbstract();

    boolean isFinal();

    boolean isStatic();

    default boolean isNative() {
        return true;
    }
}