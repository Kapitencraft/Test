package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.exe.VarTypeManager;

import java.util.Map;

public interface ScriptedInterface extends ScriptedClass {

    @Override
    default Map<String, ? extends ScriptedField> getFields() {
        return Map.of();
    }

    @Override
    default ClassReference getFieldType(String name) {
        return VarTypeManager.VOID.reference();
    }

    @Override
    default boolean hasField(String name) {
        return false;
    }

    @Override
    default short getModifiers() {
        return Modifiers.INTERFACE;
    }

    @Override
    default ClassReference superclass() {
        return VarTypeManager.VOID.reference();
    }
}
