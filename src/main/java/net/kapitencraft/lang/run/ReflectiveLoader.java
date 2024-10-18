package net.kapitencraft.lang.run;

import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.clazz.ReflectiveClass;

import java.util.HashMap;
import java.util.Map;

public class ReflectiveLoader {
    private final Map<Class<?>, LoxClass> registeredReflective = new HashMap<>();

    public PreviewClass register(Class<?> clazz, String pck) {
        PreviewClass previewClass = new PreviewClass(clazz.getSimpleName());
        registeredReflective.putIfAbsent(clazz, previewClass);
        if (!registeredReflective.containsKey(clazz.getSuperclass()) && clazz.getSuperclass() != Object.class)
            register(clazz.getSuperclass(), pck);
        return previewClass;
    }

    public void load() {
        for (Map.Entry<Class<?>, LoxClass> entry : registeredReflective.entrySet()) {
            try {
                entry.setValue(new ReflectiveClass<>(entry.getKey()));
            } catch (Exception e) {
                System.err.printf("unable to reflectively load class '%s': %s", entry.getKey().getSimpleName(), e.getMessage());
            }
        }
    }
}
