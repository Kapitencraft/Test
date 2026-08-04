package net.kapitencraft.lang.exe.natives.impl;

import net.kapitencraft.lang.exe.VirtualMachine;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.exe.natives.NativeClassInstance;
import net.kapitencraft.lang.exe.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.exe.natives.NativeClassLoader;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NativeMethod implements ScriptedCallable {
    private final ClassReference type;
    private final ClassReference[] args;
    private final ClassReference[] thrown;
    private final Method method;
    private final boolean instance;
    private final short modifiers;

    public NativeMethod(ClassReference type, ClassReference[] args, ClassReference[] thrown, Method method, boolean instance, short modifiers) {
        this.type = type;
        this.args = args;
        this.thrown = thrown;
        this.method = method;
        this.instance = instance;
        this.modifiers = modifiers;
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

            Object value = method.invoke(instance ? NativeClassLoader.extractNative(arguments[0]) : null, NativeClassLoader.extractNatives(arguments, instance));
            ScriptedClass scriptedClass = type.get();
            if (scriptedClass == VarTypeManager.VOID) {
                return null;
            } else if (scriptedClass instanceof PrimitiveClass) {
                return value;
            }
            return new NativeClassInstance((NativeClassImpl) scriptedClass, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            VirtualMachine.handleException(VirtualMachine.createException(VarTypeManager.FUNCTION_CALL_ERROR, e.getMessage()));
        } catch (Throwable e) {
            VirtualMachine.handleException(VirtualMachine.createException(VarTypeManager.UNKNOWN_ERROR, e.getMessage()));
        }
        return null;
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

    @Override
    public short modifiers() {
        return modifiers;
    }

    @Override
    public ClassReference[] thrown() {
        return thrown;
    }
}
