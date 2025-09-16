package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.oop.clazz.ScriptedClass;

public interface ClassInstance {

    void assignField(String name, Object val);

    Object getField(String name);

    ScriptedClass getType();
}
