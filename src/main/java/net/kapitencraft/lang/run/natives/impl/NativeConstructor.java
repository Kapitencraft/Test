package net.kapitencraft.lang.run.natives.impl;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.exception.runtime.AbstractScriptedException;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.natives.NativeClassLoader;
import net.kapitencraft.lang.run.VarTypeManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class NativeConstructor implements ScriptedCallable {
    private final ClassReference type;
    private final ClassReference[] args;
    private final Constructor<?> constructor;

    public NativeConstructor(ClassReference type, ClassReference[] args, Constructor<?> constructor) {
        this.type = type;
        this.args = args;
        this.constructor = constructor;
    }

    @Override
    public ClassReference type() {
        return type;
    }

    @Override
    public ClassReference[] argTypes() {
        return args;
    }

    @Override
    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
        try {
            return constructor.newInstance(NativeClassLoader.extractNatives(arguments));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw AbstractScriptedException.createException(VarTypeManager.FUNCTION_CALL_ERROR, e.getMessage());
        }
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return false;
    }
}
