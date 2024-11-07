package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.oop.clazz.LoxClass;

public abstract class NativeField extends LoxField {
    private final LoxClass type;
    private final boolean isFinal;

    protected NativeField(LoxClass type, boolean isFinal) {
        this.type = type;
        this.isFinal = isFinal;
    }

    @Override
    public LoxClass getType() {
        return type;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }
}
