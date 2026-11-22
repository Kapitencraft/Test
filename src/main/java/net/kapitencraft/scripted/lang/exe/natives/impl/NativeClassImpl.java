package net.kapitencraft.scripted.lang.exe.natives.impl;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.natives.NativeClassLoader;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.scripted.lang.oop.field.NativeField;
import net.kapitencraft.scripted.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.scripted.lang.oop.method.map.GeneratedMethodMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ApiStatus.Internal
public record NativeClassImpl(String name, String pck,
                              GeneratedMethodMap methods, Map<String, NativeField> fields, ClassReference superclass,
                              ClassReference[] interfaces, int modifiers) implements ScriptedClass {
    @ApiStatus.Internal
    public NativeClassImpl(String name, String pck,
                           Map<String, DataMethodContainer> methods, Map<String, NativeField> fields,
                           ClassReference superclass, ClassReference[] interfaces, int modifiers) {
        this(name, pck, new GeneratedMethodMap(methods), fields, superclass, interfaces, modifiers);
    }

    @Override
    public @Nullable ClassReference superclass() {
        return superclass;
    }

    @Override
    public @NotNull ClassReference getFieldType(String name) {
        return fields.containsKey(name) ? fields.get(name).type() : VarTypeManager.VOID.reference();
    }

    @Override
    public @Nullable ScriptedClass getFieldDeclaring(String name) {
        return fields.containsKey(name) ? this : ScriptedClass.super.getFieldDeclaring(name);
    }

    @Override
    public Annotation[] annotations() {
        return new Annotation[0];
    }

    @Override
    public Object getStaticField(String name) {
        return fields.get(name).get(null);
    }

    @Override
    public Object setStaticField(String name, Object val) {
        fields.get(name).set(null, NativeClassLoader.extractNative(val));
        return val;
    }

    @Override
    public boolean isNative() {
        return true;
    }
}
