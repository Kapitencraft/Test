package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;

public class RuntimeField implements ScriptedField {
    private final ClassReference type;
    private final short modifiers;

    public RuntimeField(ClassReference type, short modifiers) {
        this.type = type;
        this.modifiers = modifiers;
    }

    @Override
    public ClassReference type() {
        return type;
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(this.modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(this.modifiers);
    }

    @Override
    public short modifiers() {
        return modifiers;
    }
}
