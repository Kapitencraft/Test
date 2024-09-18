package net.kapitencraft.lang.oop;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.func.LoxCallable;

import java.util.List;
import java.util.Map;

public class LoxClass {
    final Map<String, LoxCallable> methods;
    final List<LoxClass> capsuled;
    final Map<String, LoxField> fields;
    final LoxClass superclass;

    final String name;

    public LoxClass(Map<String, LoxCallable> methods, List<LoxClass> capsuled, Map<String, LoxField> fields, LoxClass superclass, String name) {
        this.methods = methods;
        this.capsuled = capsuled;
        this.fields = fields;
        this.superclass = superclass == null && this != VarTypeManager.OBJECT ? VarTypeManager.OBJECT : superclass;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String name() {
        return name;
    }

    public LoxClass getSuperclass() {
        return superclass;
    }

    public LoxClass getFieldType(String name) {
        return fields.get(name).getType();
    }

    public boolean hasField(String name) {
        return fields.containsKey(name);
    }
}