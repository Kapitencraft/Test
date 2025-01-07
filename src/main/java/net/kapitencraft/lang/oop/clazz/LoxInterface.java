package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface LoxInterface extends LoxClass {

    @Override
    default MethodContainer getConstructor() {
        return null;
    }

    @Override
    default Map<String, ScriptedField> getFields() {
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
    default @Nullable ClassReference superclass() {
        return ClassReference.of(VarTypeManager.VOID);
    }
}
