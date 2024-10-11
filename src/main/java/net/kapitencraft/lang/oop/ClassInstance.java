package net.kapitencraft.lang.oop;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.tool.Math;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassInstance {
    private final Environment environment;
    private final Map<String, Object> fields = new HashMap<>();
    private final LoxClass type;

    public LoxClass getType() {
        return type;
    }

    public ClassInstance(LoxClass type, Interpreter interpreter) {
        this.environment = new Environment();
        environment.defineVar("this", this);
        this.type = type;
        this.executeConstructor(interpreter);
    }

    private void executeConstructor(Interpreter interpreter) {
        this.type.getFields().forEach((string, loxField) -> this.fields.put(string, loxField.initialize(this.environment, interpreter)));
    }

    public Object assignField(String name, Object val) {
        this.fields.put(name, val);
        return getField(name);
    }

    public Object assignFieldWithOperator(String name, Object val, Token type) {
        return this.assignField(name, Math.merge(getField(name), val, type));
    }

    public Object specialAssign(String name, Token assignType) {
        Object val = this.fields.get(name);
        if (val instanceof Integer) {
            this.assignField(name, (int)val + (assignType.type() == TokenType.GROW ? 1 : -1));
        } else {
            this.assignField(name, (double)val + (assignType.type() == TokenType.GROW ? 1 : -1));
        }
        return getField(name);
    }


    public Object getField(String name) {
        return this.fields.get(name);
    }

    public void construct(List<Object> params, int ordinal, Interpreter interpreter) {
        type.getConstructor().getMethodByOrdinal(ordinal).call(this.environment, interpreter, params);
    }

    public Object executeMethod(String name, int ordinal, List<Object> arguments, Interpreter interpreter) {
        LoxCallable callable = type.getMethodByOrdinal(name, ordinal);
        this.environment.push();
        try {
            return callable.call(this.environment, interpreter, arguments);
        } finally {
            this.environment.pop();
        }
    }
}
