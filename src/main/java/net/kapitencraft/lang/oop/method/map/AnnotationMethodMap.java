package net.kapitencraft.lang.oop.method.map;

import com.google.common.collect.ImmutableMap;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationMethodMap implements AbstractMethodMap {
    private final Map<String, AnnotationCallable> methods;
    private final Map<String, DataMethodContainer> forLookup;
    private final List<String> abstracts;

    public AnnotationMethodMap(Map<String, AnnotationCallable> methods) {
        this.methods = methods;
        Map<String, DataMethodContainer> map = new HashMap<>();
        List<String> abstractMethodKeys = new ArrayList<>();
        methods.forEach((string, annotationCallable) -> {
            map.put(string, new DataMethodContainer(new ScriptedCallable[]{annotationCallable}));
            if (annotationCallable.isAbstract()) abstractMethodKeys.add(string);
        });
        abstracts = abstractMethodKeys;
        this.forLookup = ImmutableMap.copyOf(map);
    }

    @Override
    public int getMethodOrdinal(String name, ClassReference[] args) {
        return args.length == 0 && methods.containsKey(name) ? 0 : -1;
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

    @Override
    public @Nullable DataMethodContainer get(String name) {
        return forLookup.get(name);
    }

    protected Map<String, AnnotationCallable> getMethods() {
        return methods;
    }

    public List<String> getAbstracts() {
        return abstracts;
    }
}
