package net.kapitencraft.lang.oop.method.map;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;

import java.util.List;
import java.util.Map;

public interface AbstractMethodMap {

    int getMethodOrdinal(String name, List<? extends LoxClass> args);

    ScriptedCallable getMethodByOrdinal(String name, int ordinal);

    boolean has(String name);

    Map<String, DataMethodContainer> asMap();
}
