package net.kapitencraft.lang.oop;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Package {
    private final String name;
    private final Map<String, ClassReference> classes = new HashMap<>();
    private final Map<String, Package> packages = new HashMap<>();

    public Package(String name) {
        this.name = name;
    }

    public boolean hasPackage(String name) {
        return packages.containsKey(name);
    }

    public boolean hasClass(String name) {
        return classes.containsKey(name);
    }

    public Package getPackage(String name) {
        return packages.get(name);
    }

    public ClassReference getClass(String name) {
        return classes.get(name);
    }

    public void addClass(String name, ScriptedClass cl) {
        if (!classes.containsKey(name))
            classes.put(name, ClassReference.of(cl));
        else
            classes.get(name).setTarget(cl);
    }

    public void addClass(String name, ClassReference reference) {
        this.classes.put(name, reference);
    }

    //use getOrCreatePackage instead
    private void addPackage(String name, Package pck) {
        packages.put(name, pck);
    }

    public Package getOrCreatePackage(String name) {
        if (!hasPackage(name)) {
            addPackage(name, new Package(this.name + "." + name));
        }
        return getPackage(name);
    }

    public Collection<ClassReference> allClasses() {
        return classes.values();
    }

    public Collection<Package> allPackages() {
        return packages.values();
    }

    public String getName() {
        return name;
    }
}
