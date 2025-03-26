package net.kapitencraft.lang.run.natives.impl;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.field.NativeField;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.natives.NativeClassLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ApiStatus.Internal
public class NativeClassImpl implements ScriptedClass {
    private final GeneratedMethodMap methods, staticMethods;
    private final Map<String, NativeField> fields, staticFields;
    private final DataMethodContainer constructor;
    private final ClassReference superclass;
    private final ClassReference[] interfaces;
    private final short modifiers;
    private final String name, pck;

    @ApiStatus.Internal
    public NativeClassImpl(String name, String pck,
                           Map<String, DataMethodContainer> staticMethods, Map<String, NativeField> staticFields,
                           Map<String, DataMethodContainer> methods, Map<String, NativeField> fields,
                           DataMethodContainer constructor, ClassReference superclass, ClassReference[] interfaces, short modifiers) {
        this.name = name;
        this.pck = pck;
        this.methods = new GeneratedMethodMap(methods);
        this.staticMethods = new GeneratedMethodMap(staticMethods);
        this.fields = fields;
        this.staticFields = staticFields;
        this.constructor = constructor;
        this.superclass = superclass;
        this.interfaces = interfaces;
        this.modifiers = modifiers;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String pck() {
        return pck;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return superclass;
    }

    @Override
    public ClassReference getFieldType(String name) {
        return fields.containsKey(name) ? fields.get(name).getType() : null;
    }

    @Override
    public boolean hasField(String name) {
        return fields.containsKey(name);
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return staticMethods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, ClassReference[] args) {
        return staticMethods.getMethodOrdinal(name, args);
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.has(name);
    }

    @Override
    public MethodContainer getConstructor() {
        return constructor;
    }

    @Override
    public short getModifiers() {
        return modifiers;
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return methods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, ClassReference[] types) {
        return methods.getMethodOrdinal(name, types);
    }

    @Override
    public boolean hasEnclosing(String name) {
        return false;
    }

    @Override
    public ClassReference getEnclosing(String name) {
        return null;
    }

    @Override
    public boolean hasMethod(String name) {
        return methods.has(name) || superclass != null && superclass.get().hasMethod(name);
    }

    @Override
    public Map<String, NativeField> getFields() {
        return fields;
    }

    @Override
    public GeneratedMethodMap getMethods() {
        return methods;
    }

    @Override
    public AnnotationClassInstance[] annotations() {
        return new AnnotationClassInstance[0];
    }

    @Override
    public ClassReference[] interfaces() {
        return interfaces;
    }

    @Override
    public Object getStaticField(String name) {
        return staticFields.get(name).get(null);
    }

    @Override
    public Object assignStaticField(String name, Object val) {
        staticFields.get(name).set(null, NativeClassLoader.extractNative(val));
        return val;
    }

    @Override
    public boolean hasInit() {
        return true;
    }

    @Override
    public void setInit() {

    }

    @Override
    public ClassReference[] enclosed() {
        return new ClassReference[0];
    }

    @Override
    public Map<String, ? extends ScriptedField> staticFields() {
        return Map.of();
    }

    @Override
    public Object assignStaticFieldWithOperator(String name, Object val, Token type, ScriptedClass executor, Operand operand) {
        Object newVal = Interpreter.INSTANCE.visitAlgebra(getStaticField(name), val, executor, type, operand);
        return assignStaticField(name, newVal);
    }

    @Override
    public Object staticSpecialAssign(String name, Token assignType) {
        return null;// super.staticSpecialAssign(name, assignType);
    }
}
