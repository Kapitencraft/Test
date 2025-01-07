package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.Interpreter;

public class SkeletonField extends ScriptedField {
    private final ClassReference type;
    private final boolean isFinal;

    public SkeletonField(ClassReference type, boolean isFinal) {
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
