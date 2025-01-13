package net.kapitencraft.lang.run.natives.impl;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.run.Interpreter;

public class NativeFieldImpl extends ScriptedField {
    private final ClassReference type;
    private final boolean isFinal;

    public NativeFieldImpl(ClassReference type, boolean isFinal) {
        this.type = type;
        this.isFinal = isFinal;
    }

    @Override
    public Object initialize(Environment environment, Interpreter interpreter) {
        return null;
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
