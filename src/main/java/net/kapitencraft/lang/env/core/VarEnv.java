package net.kapitencraft.lang.env.core;

import net.kapitencraft.lang.run.RuntimeError;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.env.abst.Leveled;
import net.kapitencraft.tool.Math;

import static net.kapitencraft.lang.run.Interpreter.checkNumberOperands;

public class VarEnv extends Leveled<String, VarEnv.Wrapper> {

    VarEnv() {
    }

    public void define(String name, Object value) {
        this.addValue(name, new Wrapper(value));
    }

    public Object get(String name) {
        return getValue(name).val;
    }

    public void assign(String name, Object value) {
        getValue(name).val = value;
    }

    public Object assignWithOperator(Token type, String name, Object value) {
        Object current = get(name);
        this.assign(name, switch (type.type) {
            case SUB_ASSIGN:
                checkNumberOperands(type, current, value);
                yield Math.mergeSub(current, value);
            case DIV_ASSIGN:
                checkNumberOperands(type, current, value);
                yield Math.mergeDiv(current, value);
            case MUL_ASSIGN:
                checkNumberOperands(type, current, value);
                yield Math.mergeMul(current, value);
            case MOD_ASSIGN:
                checkNumberOperands(type, current, value);
                yield Math.mergeMod(current, value);
            case ADD_ASSIGN:
                if (current instanceof String lS) {
                    yield lS + value;
                } else if (value instanceof String vS) {
                    yield current + vS;
                }

                try {
                    yield Math.mergeAdd(current, value);
                } catch (Exception e) {
                    throw new RuntimeError(type, "Operands must be two numbers or two strings.");
                }

            default:
                throw new RuntimeError(type, "Unknown Operation type");
        });
        return get(name);
    }

    public Object specialAssign(String name, TokenType type) {
        Object value = get(name);
        if (value instanceof Integer) {
            this.assign(name, (int) value + (type == TokenType.GROW ? 1 : -1));
        } else
            this.assign(name, (double) value + (type == TokenType.GROW ? 1 : -1));
        return get(name);
    }

    static class Wrapper {
        Object val;

        public Wrapper(Object in) {
            val = in;
        }
    }
}