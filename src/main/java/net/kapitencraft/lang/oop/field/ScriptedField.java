package net.kapitencraft.lang.oop.field;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.Interpreter;

public interface ScriptedField {

    Object initialize(Environment environment, Interpreter interpreter);

    ClassReference type();

    boolean isFinal();
}