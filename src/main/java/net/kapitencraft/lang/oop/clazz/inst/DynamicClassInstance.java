package net.kapitencraft.lang.oop.clazz.inst;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
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
        public Object getField(String name) {
            return DynamicClassInstance.this.getField(name);
        }

        @Override
        public void construct(Object[] params, int ordinal) {
            throw new IllegalAccessError("can not construct super class");
        }

        @Override
        public Object executeMethod(String name, int ordinal, Object[] arguments) {
            ScriptedCallable callable = clazz.getMethodByOrdinal(name, ordinal);
            DynamicClassInstance.this.environment.push();
            try {
                return callable.call(arguments);
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

    public Object getField(String name) {
        return this.fields.get(name);
    }

    public void construct(Object[] params, int ordinal) {
        type.getConstructor().getMethodByOrdinal(ordinal).call(params);
    }

    public Object executeMethod(String name, int ordinal, Object[] arguments) {
        ScriptedCallable callable = type.getMethodByOrdinal(name, ordinal);
        this.environment.push();
        try {
            return callable.call(arguments);
        } finally {
            this.environment.pop();
        }
    }

    @Override
    public String toString() {
        return (String) this.type.getMethod("toString", new ClassReference[0]).call(new Object[0]);
    }
}
