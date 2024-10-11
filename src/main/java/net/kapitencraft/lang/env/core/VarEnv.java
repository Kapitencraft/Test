package net.kapitencraft.lang.env.core;

import net.kapitencraft.lang.exception.runtime.MissingVarException;
import net.kapitencraft.lang.run.RuntimeError;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.env.abst.Leveled;
import net.kapitencraft.tool.Math;

import java.util.concurrent.ExecutionException;

import static net.kapitencraft.lang.run.Interpreter.checkNumberOperand;
import static net.kapitencraft.lang.run.Interpreter.checkNumberOperands;

public class VarEnv extends Leveled<String, VarEnv.Wrapper> {

    VarEnv() {
    }

    public void define(String name, Object value) {
        this.addValue(name, new Wrapper(value));
    }

    public Object get(String name) {
        try {
            return getValue(name).val;
        } catch (NullPointerException e) {
            throw new MissingVarException(name);
        }
    }

    public void assign(String name, Object value) {
        getValue(name).val = value;
    }

    public Object assignWithOperator(Token type, String name, Object value) {
        Object current = get(name);
        this.assign(name, Math.merge(current, value, type));
        return get(name);
    }

    public Object specialAssign(String name, Token type) {
        Object value = get(name);
        checkNumberOperand(type, value);
        if (value instanceof Integer) {
            this.assign(name, (int) value + (type.type() == TokenType.GROW ? 1 : -1));
        } else if (value instanceof Double)
            this.assign(name, (double) value + (type.type() == TokenType.GROW ? 1 : -1));
        return get(name);
    }

    static class Wrapper {
        Object val;

        public Wrapper(Object in) {
            val = in;
        }
    }
}