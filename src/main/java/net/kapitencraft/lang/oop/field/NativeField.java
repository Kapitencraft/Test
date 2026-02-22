package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.exe.natives.NativeClassInstance;
import net.kapitencraft.lang.exe.natives.impl.NativeClassImpl;

import java.lang.reflect.Field;

public class NativeField implements ScriptedField {
    private final ClassReference type;
    private final short modifiers;
    private final Field field;

    public NativeField(ClassReference type, short modifiers, Field field) {
        this.type = type;
        this.modifiers = modifiers;
        this.field = field;
    }

    public Object get(Object obj) {
        try {
            return new NativeClassInstance((NativeClassImpl) type.get(), field.get(obj));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(Object obj, Object val) {
        try {
            field.set(obj, val);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ClassReference type() {
        return type;
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(modifiers);
    }

    @Override
    public short modifiers() {
        return modifiers;
    }
}
