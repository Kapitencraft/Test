package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.Interpreter;

public abstract class ScriptedField {

    public abstract Object initialize(Environment environment, Interpreter interpreter);

    public abstract ClassReference getType();

    public abstract boolean isFinal();
}
