package net.kapitencraft.lang.run.natives.impl;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
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
    private final GeneratedMethodMap methods;
    private final Map<String, NativeField> fields, staticFields;
    private final DataMethodContainer constructor;
    private final ClassReference superclass;
    private final ClassReference[] interfaces;
    private final short modifiers;
    private final String name, pck;

    @ApiStatus.Internal
    public NativeClassImpl(String name, String pck,
                           Map<String, NativeField> staticFields,
                           Map<String, DataMethodContainer> methods, Map<String, NativeField> fields,
                           DataMethodContainer constructor, ClassReference superclass, ClassReference[] interfaces, short modifiers) {
        this.name = name;
        this.pck = pck;
        this.methods = new GeneratedMethodMap(methods);
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
        return fields.containsKey(name) ? fields.get(name).type() : null;
    }

    @Override
    public boolean hasField(String name) {
        return fields.containsKey(name);
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return methods.getMethod(signature);
    }

    @Override
    public short getModifiers() {
        return modifiers;
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
    public RuntimeAnnotationClassInstance[] annotations() {
        return new RuntimeAnnotationClassInstance[0];
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
    public ClassReference[] enclosed() {
        return new ClassReference[0];
    }

    @Override
    public Map<String, ? extends ScriptedField> staticFields() {
        return Map.of();
    }

    @Override
    public Object staticSpecialAssign(String name, TokenType assignType) {
        return ScriptedClass.super.staticSpecialAssign(name, assignType);
    }
}
