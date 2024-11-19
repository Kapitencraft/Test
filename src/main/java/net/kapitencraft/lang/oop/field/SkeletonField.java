package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;

public class SkeletonField extends LoxField {
    private final LoxClass type;
    private final boolean isFinal;

    public SkeletonField(LoxClass type, boolean isFinal) {
        this.type = type;
        this.isFinal = isFinal;
    }

    @Override
    public Object initialize(Environment environment, Interpreter interpreter) {
        return null;
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
