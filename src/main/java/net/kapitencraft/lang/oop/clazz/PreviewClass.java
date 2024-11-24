package net.kapitencraft.lang.oop.clazz;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;
import java.util.Map;

public class PreviewClass implements LoxClass {
    private LoxClass target;
    private final String name;
    private final boolean isInterface;

    public PreviewClass(String name, boolean isInterface) {
        this.name = name;
        this.isInterface = isInterface;
    }

    @Override
    public boolean isArray() {
        assertApplied();
        return target.isArray();
    }

    public <T extends LoxClass> T apply(T target) {
        this.target = target;
        return target;
    }

    @Override
    public ScriptedCallable getStaticMethod(String name, List<? extends LoxClass> args) {
        assertApplied();
        return target.getStaticMethod(name, args);
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        assertApplied();
        return target.getStaticMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        assertApplied();
        return target.getStaticMethodOrdinal(name, args);
    }

    @Override
    public ScriptedCallable getMethod(String name, List<LoxClass> args) {
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
    public boolean hasInit() {
        assertApplied();
        return target.hasInit();
    }

    @Override
    public void setInit() {
        assertApplied();
        target.setInit();
    }

    @Override
    public Map<String, ? extends LoxField> staticFields() {
        assertApplied();
        return target.staticFields();
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
    public LoxClass[] interfaces() {
        assertApplied();
        return target.interfaces();
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
    public boolean isAbstract() {
        assertApplied();
        return target.isAbstract();
    }

    @Override
    public boolean isFinal() {
        assertApplied();
        return target.isFinal();
    }

    @Override
    public boolean isInterface() {
        return isInterface || target != null && target.isInterface();
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
    public MethodMap getMethods() {
        assertApplied();
        return target.getMethods();
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        assertApplied();
        return target.getMethodByOrdinal(name, ordinal);
    }
}
