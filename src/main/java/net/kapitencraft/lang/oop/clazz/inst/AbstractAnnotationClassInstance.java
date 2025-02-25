package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.AbstractAnnotationClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface AbstractAnnotationClassInstance extends AbstractClassInstance {

    @Override
    default Object assignField(String name, Object val) {
        throw new IllegalAccessError("can not assign field of annotation");
    }

    @Override
    default Object assignFieldWithOperator(String name, Object val, Token type, ScriptedClass executor, Operand operand) {
        throw new IllegalAccessError("can not assign field of annotation");
    }

    @Override
    default Object specialAssign(String name, Token assignType) {
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
