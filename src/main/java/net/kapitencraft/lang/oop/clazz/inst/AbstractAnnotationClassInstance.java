package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.algebra.Operand;

public interface AbstractAnnotationClassInstance extends ClassInstance {

    @Override
    default Object assignField(String name, Object val) {
        throw new IllegalAccessError("can not assign field of annotation");
    }

    @Override
    default Object getField(String name) {
        throw new IllegalAccessError("can not get field of annotation");
    }

    @Override
    default void construct(Object[] params, int ordinal) {
        throw new IllegalAccessError("can not construct annotation");
    }

    @Override
    default Object executeMethod(String name, int ordinal, Object[] arguments) {
        return getProperty(name);
    }

    Object getProperty(String name);
}
