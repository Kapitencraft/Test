package net.kapitencraft.scripted.lang.oop.method.map;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.ClassBuilder;
import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.scripted.lang.exe.ScriptedCallable;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GeneratedMethodMap implements AbstractMethodMap {
    private final Map<String, DataMethodContainer> methods;
    private final Map<String, ScriptedCallable> plainMap;

    public GeneratedMethodMap(Map<String, DataMethodContainer> methods) {
        this.methods = methods;
        this.plainMap = ScriptedCallable.parseMethods(methods);
    }

    public static GeneratedMethodMap empty() {
        return new GeneratedMethodMap(Map.of());
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return plainMap.get(signature);
    }

    public boolean has(String name) {
        return methods.containsKey(name);
    }

    public Map<String, DataMethodContainer> asMap() {
        return methods;
    }

    @Override
    public @Nullable DataMethodContainer get(String name) {
        return methods.get(name);
    }
}
