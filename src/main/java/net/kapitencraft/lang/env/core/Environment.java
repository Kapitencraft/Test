package net.kapitencraft.lang.env.core;

import net.kapitencraft.lang.holder.token.RuntimeToken;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.algebra.Operand;

public class Environment {
    private final VarEnv vars;

    public Environment() {
        this.vars = new VarEnv();
    }

    /**
     * @return the variable added as "this", or throws when it can't be found
     */
    public Object getThis() {
        return getVar(RuntimeToken.createNative("this"));
    }

    public void push() {
        vars.push();
    }

    public void pop() {
        vars.pop();
    }

    //var
    public void defineVar(String name, Object value) {
        vars.define(name, value);
    }

    public Object getVar(RuntimeToken name) {
        return vars.get(name);
    }

    public void assignVar(RuntimeToken name, Object value) {
        vars.assign(name, value);
    }

    public Object assignVarWithOperator(TokenType type, int line, RuntimeToken name, Object value, ScriptedClass executor, Operand operand) {
        return vars.assignWithOperator(type, line, name, value, executor, operand);
    }

    public Object specialVarAssign(RuntimeToken name, TokenType type) {
        return vars.specialAssign(name, type);
    }
}
