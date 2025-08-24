package net.kapitencraft.lang.oop.method.map;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
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

    public JsonObject save(CacheBuilder builder) {
        JsonObject methods = new JsonObject();
        this.methods.forEach((name, container) -> methods.add(name, container.cache(builder)));
        return methods;
    }

    public int getMethodOrdinal(String name, ClassReference[] args) {
        return methods.containsKey(name) ? methods.get(name).getMethodOrdinal(args) : -1;
    }

    @Nullable
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        DataMethodContainer container = methods.get(name);
        return container == null ? null : container.getMethodByOrdinal(ordinal);
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
