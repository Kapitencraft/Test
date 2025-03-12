package net.kapitencraft.lang.oop.method.builder;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;

import java.util.List;

public interface MethodContainer {

    ScriptedCallable getMethod(ClassReference[] expectedArgs);

    ScriptedCallable getMethodByOrdinal(int ordinal);

    int getMethodOrdinal(ClassReference[] types);

    ScriptedCallable[] getMethods();
}
