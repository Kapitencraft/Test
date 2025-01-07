package net.kapitencraft.lang.func;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;

public interface ScriptedCallable {

    ClassReference type();

    List<ClassReference> argTypes();

    Object call(Environment environment, Interpreter interpreter, List<Object> arguments);

    boolean isAbstract();

    boolean isFinal();
}