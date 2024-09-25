package net.kapitencraft.lang.func;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;

public interface LoxCallable {
    int arity();

    LoxClass type();

    List<? extends LoxClass> argTypes();

    Object call(Environment environment, Interpreter interpreter, List<Object> arguments);

    boolean isAbstract();
}