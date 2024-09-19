package net.kapitencraft.lang.oop;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.token.TokenTypeCategory;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.Math;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassInstance {
    private final Environment environment;
    private final Map<String, Object> fields = new HashMap<>();
    private final LoxClass type;

    public ClassInstance(LoxClass type, Interpreter interpreter) {
        this.environment = new Environment();
        environment.assignVar("this", this); //oh, wow
        this.type = type;
        this.executeConstructor(interpreter);

    }

    private void executeConstructor(Interpreter interpreter) {
        this.type.fields.forEach((string, loxField) -> this.fields.put(string, loxField.initialize(this.environment, interpreter)));
    }

    public boolean hasMethod(String name) {
        return type.methods.containsKey(name);
    }

    public boolean hasField(String name) {
        return type.fields.containsKey(name);
    }

    public Object runMethod(Interpreter interpreter, String name, List<Object> args) {
        LoxCallable callable = type.methods.get(name);
        this.environment.push();
        Object val = callable.call(this.environment, interpreter, args);
        this.environment.pop();
        return val;
    }

    public Object assignField(String name, Object val) {
        this.fields.put(name, val);
        return getField(name);
    }

    public Object assignFieldWithOperator(String name, Object val, Token type) {
        this.assignField(name, Math.merge(getField(name), val, type));
        return getField(name);
    }

    public Object getField(String name) {
        return this.fields.get(name);
    }

    public void construct(List<Expr> params, Interpreter interpreter) {
        type.constructor.call(this.environment, interpreter, interpreter.visitArgs(params));
    }
}
