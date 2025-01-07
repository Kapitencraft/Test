package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.LoxClass;

public abstract class NativeField extends ScriptedField {
    private final ClassReference type;
    private final boolean isFinal;

    protected NativeField(ClassReference type, boolean isFinal) {
        this.type = type;
        this.isFinal = isFinal;
    }

    @Override
    public ClassReference getType() {
        return type;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }
}
