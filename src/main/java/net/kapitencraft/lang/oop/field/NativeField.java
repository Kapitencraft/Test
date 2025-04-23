package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.natives.NativeClassInstance;
import net.kapitencraft.lang.run.natives.impl.NativeClassImpl;

import java.lang.reflect.Field;

public class NativeField implements ScriptedField {
    private final ClassReference type;
    private final boolean isFinal;
    private final Field field;

    public NativeField(ClassReference type, boolean isFinal, Field field) {
        this.type = type;
        this.isFinal = isFinal;
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
    public Object initialize(Environment environment, Interpreter interpreter) {
        return null;
    }

    @Override
    public ClassReference type() {
        return type;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }
}
