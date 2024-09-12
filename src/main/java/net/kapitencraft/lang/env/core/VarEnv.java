package net.kapitencraft.lang.env.core;

import net.kapitencraft.lang.run.RuntimeError;
import net.kapitencraft.lang.ast.token.Token;
import net.kapitencraft.lang.ast.token.TokenType;
import net.kapitencraft.lang.env.abst.Leveled;

public class VarEnv extends Leveled<String, Object> {

    VarEnv() {
    }

    public void define(String name, Object value) {
        this.addValue(name, value);
    }

    public Object get(String name) {
        return getValue(name);
    }

    public void assign(String name, Object value) {
        addValue(name, value);
    }

    public boolean has(String name) {
        return this.getLast().containsKey(name);
    }

    public Object assignWithOperator(Token type, String name, Object value) {
        Object current = get(name);
        this.assign(name, switch (type.type) {
            case SUB_ASSIGN:
                checkNumberOperands(type, current, value);
                yield (double)current - (double)value;
            case DIV_ASSIGN:
                checkNumberOperands(type, current, value);
                yield (double)current / (double)value;
            case MUL_ASSIGN:
                checkNumberOperands(type, current, value);
                yield (double)current * (double)value;
            case MOD_ASSIGN:
                checkNumberOperands(type, current, value);
                yield (double)current % (double)value;
            case ADD_ASSIGN:
                if (current instanceof Double && value instanceof Double) {
                    yield (double)current + (double)value;
                }

                if (current instanceof String lS && value instanceof String rS) {
                    yield lS + rS;
                }

                throw new RuntimeError(type, "Operands must be two numbers or two strings.");
            default:
                throw new RuntimeError(type, "Unknown Operation type");
        });
        return get(name);
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    public Object specialAssign(String name, TokenType type) {
        this.assign(name, ((double) get(name)) + (type == TokenType.GROW ? 1 : -1));
        return get(name);
    }
}