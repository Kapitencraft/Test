package net.kapitencraft.lang.oop.clazz;

public interface EnumClass extends ScriptedClass {

    @Override
    default boolean hasMethod(String name) {
        return "values".equals(name) || ScriptedClass.super.hasMethod(name);
    }
}