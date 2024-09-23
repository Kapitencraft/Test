package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.oop.ClassInstance;
import net.kapitencraft.lang.oop.LoxField;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;
import java.util.Map;

//TODO unwrap
public class PreviewClass implements LoxClass {
    private LoxClass target;
    private final String name;

    public PreviewClass(String name) {
        this.name = name;
    }

    public void apply(LoxClass target) {
        this.target = target;
    }

    @Override
    public LoxCallable getStaticMethod(String name) {
        assertApplied();
        return target.getStaticMethod(name);
    }

    @Override
    public LoxCallable getMethod(String name) {
        assertApplied();
        return target.getMethod(name);
    }

    @Override
    public LoxClass getFieldType(String name) {
        assertApplied();
        return target.getFieldType(name);
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        assertApplied();
        return target.getStaticFieldType(name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public LoxClass superclass() {
        assertApplied();
        return target.superclass();
    }

    @Override
    public boolean hasField(String name) {
        assertApplied();
        return target.hasField(name);
    }

    @Override
    public LoxClass getStaticMethodType(String name) {
        assertApplied();
        return target.getStaticMethodType(name);
    }

    @Override
    public LoxClass getMethodType(String name) {
        assertApplied();
        return target.getMethodType(name);
    }

    @Override
    public boolean hasStaticMethod(String name) {
        assertApplied();
        return target.hasStaticMethod(name);
    }

    @Override
    public boolean hasMethod(String name) {
        assertApplied();
        return target.hasMethod(name);
    }

    @Override
    public ClassInstance createInst(List<Expr> params, Interpreter interpreter) {
        assertApplied();
        return target.createInst(params, interpreter);
    }

    @Override
    public Map<String, LoxField> getFields() {
        assertApplied();
        return target.getFields();
    }

    @Override
    public void callConstructor(Environment environment, Interpreter interpreter, List<Object> args) {
        assertApplied();
        target.callConstructor(environment, interpreter, args);
    }

    private void assertApplied() {
        if (target == null) throw new NullPointerException("preview not applied");
    }

    @Override
    public boolean isChildOf(LoxClass suspectedParent) {
        assertApplied();
        return target.isChildOf(suspectedParent);
    }

    @Override
    public boolean is(LoxClass other) {
        return LoxClass.super.is(other) || target != null && other.is(target);
    }

    @Override
    public boolean isParentOf(LoxClass suspectedChild) {
        assertApplied();
        return target.isParentOf(suspectedChild);
    }

    public LoxClass getTarget() {
        assertApplied();
        return target;
    }
}
