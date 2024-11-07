package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;

import java.util.Map;

public interface LoxInterface extends LoxClass {

    @Override
    default MethodContainer getConstructor() {
        return null;
    }

    @Override
    default Map<String, LoxField> getFields() {
        return Map.of();
    }

    @Override
    default LoxClass getFieldType(String name) {
        return VarTypeManager.VOID;
    }

    @Override
    default boolean hasField(String name) {
        return false;
    }

    @Override
    default boolean isAbstract() {
        return true;
    }

    @Override
    default boolean isFinal() {
        return false;
    }

    @Override
    default boolean isInterface() {
        return true;
    }

    @Override
    default LoxClass superclass() {
        return VarTypeManager.VOID;
    }
}
