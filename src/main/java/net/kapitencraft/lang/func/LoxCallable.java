package net.kapitencraft.lang.func;

import net.kapitencraft.lang.run.Interpreter;

import java.util.List;

public interface LoxCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}