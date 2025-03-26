package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.tool.Math;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicClassInstance implements ClassInstance {
    private final Environment environment;
    private final Map<String, Object> fields = new HashMap<>();
    private final ScriptedClass type;

    public ScriptedClass getType() {
        return type;
    }

    public DynamicClassInstance(ScriptedClass type, Interpreter interpreter) {
        this.environment = new Environment();
        this.type = type;
        environment.defineVar("this", this);
        ClassReference superClass = this.type.superclass();
        if (superClass != null)
            environment.defineVar("super", new SuperWrapper(superClass.get()));
        this.executeConstructor(interpreter);
    }

    private class SuperWrapper implements ClassInstance {
        private final ScriptedClass clazz;

        private SuperWrapper(ScriptedClass clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object assignField(String name, Object val) {
            return DynamicClassInstance.this.assignField(name, val);
        }

        @Override
        public Object assignFieldWithOperator(String name, Object val, int line, TokenType type, ScriptedClass executor, Operand operand) {
            return DynamicClassInstance.this.assignFieldWithOperator(name, val, line, type, executor, operand);
        }

        @Override
        public Object specialAssign(String name, TokenType assignType) {
            return DynamicClassInstance.this.specialAssign(name, assignType);
        }

        @Override
        public Object getField(String name) {
            return DynamicClassInstance.this.getField(name);
        }

        @Override
        public void construct(List<Object> params, int ordinal, Interpreter interpreter) {
            throw new IllegalAccessError("can not construct super class");
        }

        @Override
        public Object executeMethod(String name, int ordinal, List<Object> arguments, Interpreter interpreter) {
            ScriptedCallable callable = clazz.getMethodByOrdinal(name, ordinal);
            DynamicClassInstance.this.environment.push();
            try {
                return callable.call(DynamicClassInstance.this.environment, interpreter, arguments);
            } finally {
                DynamicClassInstance.this.environment.pop();
            }
        }

        @Override
        public ScriptedClass getType() {
            return clazz;
        }
    }

    private void executeConstructor(Interpreter interpreter) {
        this.type.getFields().forEach((string, loxField) -> this.fields.put(string, loxField.initialize(this.environment, interpreter)));
    }

    public Object assignField(String name, Object val) {
        this.fields.put(name, val);
        return getField(name);
    }

    public Object assignFieldWithOperator(String name, Object val, int line, TokenType type, ScriptedClass executor, Operand operand) {
        Object newVal = Interpreter.INSTANCE.visitAlgebra(getField(name), val, executor, type, line, operand);
        return this.assignField(name, newVal);
    }

    public Object specialAssign(String name, TokenType assignType) {
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
        return (String) this.type.getMethod("toString", new ClassReference[0]).call(this.environment, Interpreter.INSTANCE, List.of());
    }
}
