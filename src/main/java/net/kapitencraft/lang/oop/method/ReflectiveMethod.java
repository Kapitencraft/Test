package net.kapitencraft.lang.oop.method;

import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class ReflectiveMethod implements LoxCallable {
    private final Method method;
    private final List<LoxClass> args;
    private final LoxClass retType;

    public ReflectiveMethod(Method method) {
        this.method = method;
        this.args = Arrays.stream(method.getParameterTypes()).map(VarTypeManager::lookupClass).toList();
        this.retType = VarTypeManager.lookupClass(method.getReturnType());
    }

    @Override
    public int arity() {
        return method.getParameterCount();
    }

    @Override
    public LoxClass type() {
        return retType;
    }

    @Override
    public List<? extends LoxClass> argTypes() {
        return args;
    }

    @Override
    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
        List<Object> args = arguments;
        try {
            return method.invoke(null, arguments);
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.err.println("error accessing method '" + method.getName() + "': " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(method.getModifiers());
    }
}
