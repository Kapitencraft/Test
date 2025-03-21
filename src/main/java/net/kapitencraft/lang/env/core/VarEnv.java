package net.kapitencraft.lang.env.core;

import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.exception.runtime.AbstractScriptedException;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.env.abst.Leveled;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;

public class VarEnv extends Leveled<String, VarEnv.Wrapper> {

    VarEnv() {
    }

    public void define(String name, Object value) {
        this.addValue(name, new Wrapper(value));
    }

    public Object get(Token name) {
        try {
            return getValue(name.lexeme()).val;
        } catch (NullPointerException e) {
            Interpreter.INSTANCE.pushCallIndex(name.line());
            throw AbstractScriptedException.createException(VarTypeManager.MISSING_VAR_ERROR, "Variable '" + name.lexeme() + "' not found in current scope");
        }
    }

    public void assign(Token name, Object value) {
        try {
            getValue(name.lexeme()).val = value;
        } catch (NullPointerException e) {
            Interpreter.INSTANCE.pushCallIndex(name.line());
            throw AbstractScriptedException.createException(VarTypeManager.MISSING_VAR_ERROR, "Variable '" + name + "' not found in current scope");
        }
    }

    public Object assignWithOperator(Token type, Token name, Object value, ScriptedClass executor, Operand operand) {
        Object current = get(name);
        Object result = Interpreter.INSTANCE.visitAlgebra(current, value, executor, type, operand);
        this.assign(name, result);
        return result;
    }

    public Object specialAssign(Token name, Token type) {
        Object value = get(name);
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