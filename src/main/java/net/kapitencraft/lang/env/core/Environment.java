package net.kapitencraft.lang.env.core;

import net.kapitencraft.lang.ast.token.Token;
import net.kapitencraft.lang.ast.token.TokenType;
import net.kapitencraft.lang.func.LoxCallable;

public class Environment {
    private final VarEnv vars;
    private final MethodEnv methods;

    public Environment() {
        this.vars = new VarEnv();
        this.methods = new MethodEnv();
    }

    public void push() {
        vars.push();
        methods.push();
    }

    public void pop() {
        vars.pop();
        methods.pop();
    }

    //var
    public void defineVar(String name, Object value) {
        vars.define(name, value);
    }

    public Object getVar(String name) {
        return vars.get(name);
    }

    public void assignVar(String name, Object value) {
        vars.assign(name, value);
    }

    public Object assignVarWithOperator(Token type, String name, Object value) {
        return vars.assignWithOperator(type, name, value);
    }

    public boolean hasVar(String name) {
        return vars.hasVar(name);
    }

    //method
    public void defineMethod(String name, LoxCallable func) {
        this.methods.define(name, func);
    }

    public LoxCallable getMethod(String name) {
        return this.methods.get(name);
    }

    public Object specialVarAssign(String name, TokenType type) {
        return vars.specialAssign(name, type);
    }
}
