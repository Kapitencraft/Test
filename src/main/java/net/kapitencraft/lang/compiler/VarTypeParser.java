package net.kapitencraft.lang.compiler;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.oop.clazz.LoxClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class VarTypeParser {
    private final Map<String, ClassReference> implemented = new HashMap<>();

    public VarTypeParser() {
        implemented.putAll(VarTypeManager.getPackage("scripted.lang").allClasses().stream().map(ClassReference::get).collect(Collectors.toMap(LoxClass::name, LoxClass::reference)));
    }

    public boolean hasClass(String clazz) {
        return implemented.containsKey(clazz);
    }

    public ClassReference getClass(String clazz) {
        return implemented.get(clazz);
    }

    public void addClass(ClassReference clazz, String nameOverride) {
        implemented.put(nameOverride != null ? nameOverride : clazz.name(), clazz);
    }

    @Override
    public String toString() {
        return "VarTypeParser" + implemented;
    }

    public boolean hasClass(ClassReference target, String nameOverride) {
        return hasClass(Optional.ofNullable(nameOverride).orElseGet(target::name));
    }
}
