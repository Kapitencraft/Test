package net.kapitencraft.lang.exe.natives.impl;

import net.kapitencraft.lang.exe.VirtualMachine;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.exe.natives.NativeClassLoader;
import net.kapitencraft.lang.exe.VarTypeManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
    public ClassReference retType() {
        return type;
    }

    @Override
    public ClassReference[] argTypes() {
        return args;
    }

    @Override
    public Object call(Object[] arguments) {
        try {
            return constructor.newInstance(NativeClassLoader.extractNatives(arguments, false));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            VirtualMachine.handleException(VirtualMachine.createException(VarTypeManager.FUNCTION_CALL_ERROR, e.getMessage()));
            return null;
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

    @Override
    public boolean isStatic() {
        return false;
    }
}
