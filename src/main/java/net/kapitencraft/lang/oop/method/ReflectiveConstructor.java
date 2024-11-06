package net.kapitencraft.lang.oop.method;

import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;

import java.lang.reflect.Constructor;
import java.util.List;

public class ReflectiveConstructor<T> implements ScriptedCallable {
    private final Constructor<T> value;
    private final LoxClass target;

    public ReflectiveConstructor(Constructor<T> value) {
        this.value = value;
        this.target = VarTypeManager.lookupClass(value.getDeclaringClass());
    }

    @Override
    public LoxClass type() {
        return null;
    }

    @Override
    public List<? extends LoxClass> argTypes() {
        return List.of();
    }

    @Override
    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }
}
