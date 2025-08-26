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
    private final Map<String, Object> fields = new HashMap<>();
    private final ScriptedClass type;

    public ScriptedClass getType() {
        return type;
    }

    public DynamicClassInstance(ScriptedClass type) {
        this.type = type;
    }

    public void assignField(String name, Object val) {
        this.fields.put(name, val);
    }

    public Object getField(String name) {
        return this.fields.get(name);
    }

    @Override
    public String toString() {
        return (String) this.type.getMethod("toString()").call(new Object[0]);
    }
}
