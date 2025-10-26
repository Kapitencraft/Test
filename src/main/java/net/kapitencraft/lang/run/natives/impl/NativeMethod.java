package net.kapitencraft.lang.run.natives.impl;

import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.exception.runtime.AbstractScriptedException;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.natives.NativeClassLoader;
import net.kapitencraft.lang.run.VarTypeManager;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NativeMethod implements ScriptedCallable {
    private final ClassReference type;
    private final ClassReference[] args;
    private final Method method;
    private final boolean instance;
    private final short modifiers;

    public NativeMethod(ClassReference type, ClassReference[] args, Method method, boolean instance, short modifiers) {
        this.type = type;
        this.args = args;
        this.method = method;
        this.instance = instance;
        this.modifiers = modifiers;
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
    public Object call(Object[] arguments) {
        try {
            return method.invoke(instance ? NativeClassLoader.extractNative(arguments[0]) : null, NativeClassLoader.extractNatives(arguments, instance));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw AbstractScriptedException.createException(VarTypeManager.FUNCTION_CALL_ERROR, e.getMessage());
        } catch (Throwable e) {
            throw AbstractScriptedException.createException(NativeClassLoader.getClass(e.getClass()).orElse(VarTypeManager.UNKNOWN_ERROR), e.getMessage());
        }
    }

    @Override
    public boolean isAbstract() {
        return Modifiers.isAbstract(modifiers);
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(modifiers);
    }
}
