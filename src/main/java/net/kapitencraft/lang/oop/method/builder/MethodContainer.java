package net.kapitencraft.lang.oop.method.builder;

import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.oop.clazz.LoxClass;

import java.util.List;

public interface MethodContainer {

    LoxCallable getMethod(List<? extends LoxClass> expectedArgs);

    LoxCallable getMethodByOrdinal(int ordinal);

    int getMethodOrdinal(List<? extends LoxClass> types);
}
