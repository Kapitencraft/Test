package net.kapitencraft.lang.oop;

import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.NativeClass;
import net.kapitencraft.lang.run.Interpreter;

public class NativeClassInstance<T> extends ClassInstance {
    private final T value;

    public NativeClassInstance(LoxClass type, Interpreter interpreter, T value) {
        super(type, interpreter);
        this.value = value;
    }
}
