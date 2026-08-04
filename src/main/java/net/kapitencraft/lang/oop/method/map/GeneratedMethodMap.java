package net.kapitencraft.lang.oop.method.map;

import net.kapitencraft.lang.exe.ScriptedCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GeneratedMethodMap implements AbstractMethodMap {
    private final Map<String, DataMethodContainer> methods;
    private final Map<String, ScriptedCallable> signatureMap;

    public GeneratedMethodMap(Map<String, DataMethodContainer> methods) {
        this.methods = methods;
        this.signatureMap = ScriptedCallable.parseMethods(methods);
    }

    public static GeneratedMethodMap empty() {
        return new GeneratedMethodMap(Map.of());
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return signatureMap.get(signature);
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
