package net.kapitencraft.lang.oop;

import net.kapitencraft.lang.oop.clazz.GeneratedLoxClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Package {
    private final Map<String, LoxClass> classes = new HashMap<>();
    private final Map<String, Package> packages = new HashMap<>();

    public boolean hasPackage(String name) {
        return packages.containsKey(name);
    }

    public boolean hasClass(String name) {
        return classes.containsKey(name);
    }

    public Package getPackage(String name) {
        return packages.get(name);
    }

    public LoxClass getClass(String name) {
        return classes.get(name);
    }

    public void addClass(String name, LoxClass cl) {
        if (classes.get(name) instanceof PreviewClass previewClass) {
            previewClass.apply(cl);
            if (cl instanceof GeneratedLoxClass) classes.put(name, cl);
        } else {
            classes.put(name, cl);
        }
    }

    public void addPackage(String name, Package pck) {
        packages.put(name, pck);
    }

    public Package getOrCreatePackage(String name) {
        if (!hasPackage(name)) {
            addPackage(name, new Package());
        }
        return getPackage(name);
    }

    public Collection<LoxClass> allClasses() {
        return classes.values();
    }
}
