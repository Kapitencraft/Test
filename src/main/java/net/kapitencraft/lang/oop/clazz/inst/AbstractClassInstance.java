package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;

import java.util.List;

public interface AbstractClassInstance {

    Object assignField(String name, Object val);

    Object assignFieldWithOperator(String name, Object val, Token type, LoxClass executor, Operand operand);

    Object specialAssign(String name, Token assignType);

    Object getField(String name);

    void construct(List<Object> params, int ordinal, Interpreter interpreter);

    Object executeMethod(String name, int ordinal, List<Object> arguments, Interpreter interpreter);

    LoxClass getType();
}
