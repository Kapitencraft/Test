package net.kapitencraft.lang.oop.method.map;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface AbstractMethodMap {

    ScriptedCallable getMethod(String signature);

    boolean has(String name);

    Map<String, DataMethodContainer> asMap();

    @Nullable DataMethodContainer get(String name);
}
