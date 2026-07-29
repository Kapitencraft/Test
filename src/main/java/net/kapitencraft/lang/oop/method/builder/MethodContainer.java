package net.kapitencraft.lang.oop.method.builder;

import net.kapitencraft.lang.exe.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;

public interface MethodContainer {

    ScriptedCallable getMethod(ClassReference[] expectedArgs);

    ScriptedCallable getMethodByOrdinal(int ordinal);

    int getMethodOrdinal(ClassReference[] types);

    ScriptedCallable[] methods();
}
