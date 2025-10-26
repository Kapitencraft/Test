package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.Interpreter;

public record SkeletonField(ClassReference type, short modifiers) implements ScriptedField {

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(modifiers);
    }
}
