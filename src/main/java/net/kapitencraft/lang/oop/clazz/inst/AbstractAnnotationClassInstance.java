package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;

import java.util.List;

public interface AbstractAnnotationClassInstance extends ClassInstance {

    @Override
    default Object assignField(String name, Object val) {
        throw new IllegalAccessError("can not assign field of annotation");
    }

    @Override
    default Object assignFieldWithOperator(String name, Object val, TokenType type, int line, ScriptedClass executor, Operand operand) {
        throw new IllegalAccessError("can not assign field of annotation");
    }

    @Override
    default Object specialAssign(String name, TokenType assignType) {
        throw new IllegalAccessError("can not assign field of annotation");
    }

    @Override
    default Object getField(String name) {
        throw new IllegalAccessError("can not get field of annotation");
    }

    @Override
    default void construct(List<Object> params, int ordinal, Interpreter interpreter) {
        throw new IllegalAccessError("can not construct annotation");
    }

    @Override
    default Object executeMethod(String name, int ordinal, List<Object> arguments, Interpreter interpreter) {
        return getProperty(name);
    }

    Object getProperty(String name);
}
