package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.Interpreter;

public record SkeletonField(ClassReference type, boolean isFinal) implements ScriptedField {

    @Override
    public Object initialize(Environment environment, Interpreter interpreter) {
        return null;
    }
}
