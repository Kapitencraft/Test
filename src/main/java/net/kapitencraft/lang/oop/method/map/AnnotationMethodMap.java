package net.kapitencraft.lang.oop.method.map;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationMethodMap implements AbstractMethodMap {
    private final Map<String, AnnotationCallable> methods;
    private final Map<String, DataMethodContainer> forLookup;

    public AnnotationMethodMap(Map<String, AnnotationCallable> methods) {
        this.methods = methods;
        Map<String, DataMethodContainer> map = new HashMap<>();
        methods.forEach((string, annotationCallable) -> map.put(string, new DataMethodContainer(new ScriptedCallable[]{annotationCallable})));
        this.forLookup = ImmutableMap.copyOf(map);
    }

    @Override
    public int getMethodOrdinal(String name, List<? extends LoxClass> args) {
        return args.isEmpty() && methods.containsKey(name) ? 0 : -1;
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return ordinal == 0 ? methods.get(name) : null;
    }

    @Override
    public boolean has(String name) {
        return methods.containsKey(name);
    }

    @Override
    public Map<String, DataMethodContainer> asMap() {
        return forLookup;
    }

    protected Map<String, AnnotationCallable> getMethods() {
        return methods;
    }
}
