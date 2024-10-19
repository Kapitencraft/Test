package net.kapitencraft.lang.env.core;

import net.kapitencraft.lang.holder.token.Token;

public class Environment {
    private final VarEnv vars;

    public Environment() {
        this.vars = new VarEnv();
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

    public Object getVar(Token name) {
        return vars.get(name);
    }

    public void assignVar(Token name, Object value) {
        vars.assign(name, value);
    }

    public Object assignVarWithOperator(Token type, Token name, Object value) {
        return vars.assignWithOperator(type, name, value);
    }

    public Object specialVarAssign(Token name, Token type) {
        return vars.specialAssign(name, type);
    }
}
