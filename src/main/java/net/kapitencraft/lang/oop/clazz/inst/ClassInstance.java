package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.algebra.Operand;

public interface ClassInstance {

    void assignField(String name, Object val);

    Object getField(String name);

    ScriptedClass getType();
}
