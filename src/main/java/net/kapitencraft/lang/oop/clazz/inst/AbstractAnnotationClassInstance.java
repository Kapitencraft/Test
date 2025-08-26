package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.algebra.Operand;

public interface AbstractAnnotationClassInstance extends ClassInstance {

    @Override
    default void assignField(String name, Object val) {
        throw new IllegalAccessError("can not assign field of annotation");
    }

    @Override
    default Object getField(String name) {
        throw new IllegalAccessError("can not get field of annotation");
    }

    Object getProperty(String name);
}
