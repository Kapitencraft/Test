package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
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
    public LoxCallable getStaticMethod(String name, List<? extends LoxClass> args) {
        assertApplied();
        return target.getStaticMethod(name, args);
    }

    @Override
    public LoxCallable getStaticMethodByOrdinal(String name, int ordinal) {
        assertApplied();
        return target.getStaticMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        assertApplied();
        return target.getStaticMethodOrdinal(name, args);
    }

    @Override
    public LoxCallable getMethod(String name, List<LoxClass> args) {
        assertApplied();
        return target.getMethod(name, args);
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
    public Object getStaticField(String name) {
        assertApplied();
        return target.getStaticField(name);
    }

    @Override
    public Object assignStaticField(String name, Object val) {
        assertApplied();
        return target.assignStaticField(name, val);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String packageRepresentation() {
        assertApplied();
        return target.packageRepresentation();
    }

    @Override
    public String absoluteName() {
        assertApplied();
        return target.absoluteName();
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
    public ClassInstance createInst(List<Expr> params, int ordinal, Interpreter interpreter) {
        assertApplied();
        return target.createInst(params, ordinal, interpreter);
    }

    @Override
    public Map<String, LoxField> getFields() {
        assertApplied();
        return target.getFields();
    }

    @Override
    public MethodContainer getConstructor() {
        assertApplied();
        return target.getConstructor();
    }

    @Override
    public Map<String, MethodContainer> getMethods() {
        assertApplied();
        return target.getMethods();
    }

    @Override
    public boolean isAbstract() {
        assertApplied();
        return target.isAbstract();
    }

    @Override
    public boolean isFinal() {
        assertApplied();
        return target.isFinal();
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
    public boolean equals(Object obj) {
        return obj instanceof LoxClass cl && this.is(cl);
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

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        assertApplied();
        return target.getMethodOrdinal(name, types);
    }

    @Override
    public boolean hasEnclosing(String name) {
        assertApplied();
        return target.hasEnclosing(name);
    }

    @Override
    public LoxClass getEnclosing(String name) {
        assertApplied();
        return target.getEnclosing(name);
    }

    @Override
    public LoxCallable getMethodByOrdinal(String name, int ordinal) {
        assertApplied();
        return target.getMethodByOrdinal(name, ordinal);
    }
}
