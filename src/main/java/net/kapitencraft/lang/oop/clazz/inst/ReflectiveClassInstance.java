package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.oop.clazz.ReflectiveClass;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;

public class ReflectiveClassInstance<T> extends ClassInstance {
    private final ReflectiveClass<T> type;
    private final T value;

    public ReflectiveClassInstance(ReflectiveClass<T> type, List<Object> params, int ordinal, Interpreter interpreter) {
        super(type, interpreter);
        this.type = type;
        this.value = (T) type.getConstructor().getMethodByOrdinal(ordinal).call(null, interpreter, params);
    }

    @Override
    public void construct(List<Object> params, int ordinal, Interpreter interpreter) {
        throw new IllegalAccessError("do not call construct on reflective class instance!");
    }
}
