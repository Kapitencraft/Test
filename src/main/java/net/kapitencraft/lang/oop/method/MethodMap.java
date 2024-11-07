package net.kapitencraft.lang.oop.method;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;

import java.util.List;
import java.util.Map;

public class MethodMap {
    private final Map<String, DataMethodContainer> methods;

    public MethodMap(Map<String, DataMethodContainer> methods) {
        this.methods = methods;
    }

    public JsonObject save(CacheBuilder builder) {
        JsonObject methods = new JsonObject();
        this.methods.forEach((name, container) -> methods.add(name, container.cache(builder)));
        return methods;
    }

    public int getMethodOrdinal(String name, List<? extends LoxClass> args) {
        return methods.get(name).getMethodOrdinal(args);
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
