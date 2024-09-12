package net.kapitencraft.lang.ast;

import net.kapitencraft.lang.ast.token.Token;
import net.kapitencraft.lang.ast.token.TokenType;
import net.kapitencraft.lang.run.RuntimeError;
import net.kapitencraft.tool.Math;

import static net.kapitencraft.lang.run.Interpreter.checkNumberOperands;

public class Var<T> {
    private T value;
    private final boolean isFinal;
    private final Class<T> tClass;

    public Var(boolean isFinal, Class<T> tClass) {
        this.isFinal = isFinal;
        this.tClass = tClass;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public Class<T> getValueClass() {
        return tClass;
    }

    public T getValue() {
        return value;
    }

    public void assign(Object value) {
        this.value = (T) value;
    }

    public T assignWithOperator(Token type, Object value) {
        this.assign(switch (type.type) {
            case SUB_ASSIGN:
                checkNumberOperands(type, this.value, value);
                yield Math.mergeSub(this.value, value);
            case DIV_ASSIGN:
                checkNumberOperands(type, this.value, value);
                yield Math.mergeDiv(this.value, value);
            case MUL_ASSIGN:
                checkNumberOperands(type, this.value, value);
                yield Math.mergeMul(this.value, value);
            case MOD_ASSIGN:
                checkNumberOperands(type, this.value, value);
                yield Math.mergeMod(this.value, value);
            case ADD_ASSIGN:
                if (this.value instanceof String lS) {
                    yield lS + value;
                } else if (value instanceof String vS) {
                    yield this.value + vS;
                }

                try {
                    yield Math.mergeAdd(this.value, value);
                } catch (Exception e) {
                    throw new RuntimeError(type, "Operands must be two numbers or two strings.");
                }

            default:
                throw new RuntimeError(type, "Unknown Operation type");
        });
        return this.value;
    }

    public T specialAssign(TokenType type) {
        if (this.value instanceof Integer i) {
            this.assign((int)this.value + (type == TokenType.GROW ? 1 : -1));
        } else
            this.assign(((double) this.value) + (type == TokenType.GROW ? 1 : -1));

        return this.value;
    }
}
