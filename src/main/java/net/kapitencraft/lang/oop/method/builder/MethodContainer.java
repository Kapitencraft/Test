package net.kapitencraft.lang.oop.method.builder;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.LoxClass;

import java.util.List;

public interface MethodContainer {

    ScriptedCallable getMethod(List<? extends LoxClass> expectedArgs);

    ScriptedCallable getMethodByOrdinal(int ordinal);

    int getMethodOrdinal(List<? extends LoxClass> types);

    ScriptedCallable[] getMethods();
}
