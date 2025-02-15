package net.kapitencraft.lang.oop.method.map;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;

import java.util.List;
import java.util.Map;

public class GeneratedMethodMap implements AbstractMethodMap {
    private final Map<String, DataMethodContainer> methods;

    public GeneratedMethodMap(Map<String, DataMethodContainer> methods) {
        this.methods = methods;
    }

    public static GeneratedMethodMap empty() {
        return new GeneratedMethodMap(Map.of());
    }

    public JsonObject save(CacheBuilder builder) {
        JsonObject methods = new JsonObject();
        this.methods.forEach((name, container) -> methods.add(name, container.cache(builder)));
        return methods;
    }

    public int getMethodOrdinal(String name, List<ClassReference> args) {
        return methods.containsKey(name) ? methods.get(name).getMethodOrdinal(args) : -1;
    }

    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return methods.get(name).getMethodByOrdinal(ordinal);
    }

    public boolean has(String name) {
        return methods.containsKey(name);
    }

    public Map<String, DataMethodContainer> asMap() {
        return methods;
    }
}
