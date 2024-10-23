package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;

public abstract class LoxField {

    public abstract Object initialize(Environment environment, Interpreter interpreter);

    public abstract LoxClass getType();

    public abstract boolean isFinal();
}
