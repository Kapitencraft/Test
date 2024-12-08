package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;
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
        environment.defineVar("super", this); //TODO add scoped method map
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

    public Object assignFieldWithOperator(String name, Object val, Token type, LoxClass executor, Operand operand) {
        Object newVal = Interpreter.INSTANCE.visitAlgebra(getField(name), val, executor, type, operand);
        return this.assignField(name, newVal);
    }

    public Object specialAssign(String name, Token assignType) {
        Object val = getField(name);
        return this.assignField(name, Math.specialMerge(val, assignType));
    }

    public Object getField(String name) {
        return this.fields.get(name);
    }

    public void construct(List<Object> params, int ordinal, Interpreter interpreter) {
        type.getConstructor().getMethodByOrdinal(ordinal).call(this.environment, interpreter, params);
    }

    public Object executeMethod(String name, int ordinal, List<Object> arguments, Interpreter interpreter) {
        ScriptedCallable callable = type.getMethodByOrdinal(name, ordinal);
        this.environment.push();
        try {
            return callable.call(this.environment, interpreter, arguments);
        } finally {
            this.environment.pop();
        }
    }

    @Override
    public String toString() {
        return (String) this.type.getMethod("toString", List.of()).call(this.environment, Interpreter.INSTANCE, List.of());
    }
}
