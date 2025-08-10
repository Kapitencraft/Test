package net.kapitencraft.lang.env.core;

import net.kapitencraft.lang.holder.token.RuntimeToken;
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

    public Object get(RuntimeToken name) {
        try {
            return getValue(name.lexeme()).val;
        } catch (NullPointerException e) {
            throw AbstractScriptedException.createException(VarTypeManager.MISSING_VAR_ERROR, "Variable '" + name.lexeme() + "' not found in current scope");
        }
    }

    public void assign(RuntimeToken name, Object value) {
        try {
            getValue(name.lexeme()).val = value;
        } catch (NullPointerException e) {
            throw AbstractScriptedException.createException(VarTypeManager.MISSING_VAR_ERROR, "Variable '" + name.lexeme() + "' not found in current scope");
        }
    }

    static class Wrapper {
        Object val;

        public Wrapper(Object in) {
            val = in;
        }
    }
}