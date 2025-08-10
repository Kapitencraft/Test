package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.algebra.Operand;

public interface ClassInstance {

    Object assignField(String name, Object val);

    Object getField(String name);

    void construct(Object[] params, int ordinal);

    Object executeMethod(String name, int ordinal, Object[] arguments);

    ScriptedClass getType();
}
