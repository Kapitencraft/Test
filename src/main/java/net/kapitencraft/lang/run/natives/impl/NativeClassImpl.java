package net.kapitencraft.lang.run.natives.impl;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.wrapper.NativeUtilClass;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ApiStatus.Internal
public class NativeClassImpl extends NativeUtilClass {
    private final GeneratedMethodMap methods;
    private final Map<String, ? extends ScriptedField> fields;
    private final DataMethodContainer constructor;
    private final ClassReference superclass;
    private final ClassReference[] interfaces;
    private final short modifiers;

    @ApiStatus.Internal
    public NativeClassImpl(String name, String pck,
                           Map<String, DataMethodContainer> staticMethods, Map<String, ? extends ScriptedField> staticFields,
                           Map<String, DataMethodContainer> methods, Map<String, ? extends ScriptedField> fields,
                           DataMethodContainer constructor, ClassReference superclass, ClassReference[] interfaces, short modifiers) {
        super(staticMethods, staticFields, name, pck);
        this.methods = new GeneratedMethodMap(methods);
        this.fields = fields;
        this.constructor = constructor;
        this.superclass = superclass;
        this.interfaces = interfaces;
        this.modifiers = modifiers;
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
    public boolean hasMethod(String name) {
        return methods.has(name) || superclass != null && super.hasMethod(name);
    }

    @Override
    public Map<String, ? extends ScriptedField> getFields() {
        return fields;
    }

    @Override
    public GeneratedMethodMap getMethods() {
        return methods;
    }

    @Override
    public ClassReference[] interfaces() {
        return interfaces;
    }
}
