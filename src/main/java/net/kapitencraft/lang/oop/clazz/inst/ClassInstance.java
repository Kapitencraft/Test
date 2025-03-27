package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;

import java.util.List;

public interface ClassInstance {

    Object assignField(String name, Object val);

    Object assignFieldWithOperator(String name, Object val, TokenType type, int line, ScriptedClass executor, Operand operand);

    Object specialAssign(String name, TokenType assignType);

    Object getField(String name);

    void construct(List<Object> params, int ordinal, Interpreter interpreter);

    Object executeMethod(String name, int ordinal, List<Object> arguments, Interpreter interpreter);

    ScriptedClass getType();
}
