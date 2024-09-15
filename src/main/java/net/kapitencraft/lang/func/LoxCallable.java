package net.kapitencraft.lang.func;

import net.kapitencraft.lang.run.Interpreter;

import java.util.List;

public interface LoxCallable {
    int arity();

    Class<?> type();

    List<? extends Class<?>> argTypes();

    Object call(Interpreter interpreter, List<Object> arguments);
}