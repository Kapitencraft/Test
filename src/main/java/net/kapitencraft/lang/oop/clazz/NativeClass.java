package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.oop.ClassInstance;
import net.kapitencraft.lang.oop.NativeClassInstance;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;
import java.util.function.Function;

public abstract class NativeClass<T> implements LoxClass {
    private final Function<List<Object>, T> creator;

    protected NativeClass(Function<List<Object>, T> creator) {
        this.creator = creator;
    }

    @Override
    public ClassInstance createInst(List<Expr> params, Interpreter interpreter) {
        return new NativeClassInstance<>(this, interpreter, );
        return LoxClass.super.createInst(params, interpreter);
    }

    @Override
    public LoxClass superclass() {
        return null;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return null;
    }

    @Override
    public LoxClass getStaticMethodType(String name) {
        return null;
    }

    @Override
    public LoxCallable getStaticMethod(String name) {
        return null;
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return false;
    }

    @Override
    public void callConstructor(Environment environment, Interpreter interpreter, List<Object> args) {

    }

    @Override
    public boolean isAbstract() {
        return false;
    }
}
