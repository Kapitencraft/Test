package net.kapitencraft.lang.oop.method.map;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.ClassBuilder;
import net.kapitencraft.lang.compiler.Synthesizer;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
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

    public JsonObject save(ClassBuilder builder) {
        this.methods.forEach((s, dataMethodContainer) -> {
            for (ScriptedCallable method : dataMethodContainer.methods()) {
                builder.write16BitShort(method.modifiers());
                builder.
                builder.writeArray(s.getBytes());
                builder.writeArray(VarTypeManager.getMet);
            }
        });
        JsonObject methods = new JsonObject();
        this.methods.forEach((name, container) -> methods.add(name, container.cache(builder)));
           return methods;
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
