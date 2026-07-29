package net.kapitencraft.lang.oop.clazz.generated;

import com.google.common.collect.Multimap;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.exe.ScriptedCallable;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.lang.holder.bytecode.attributes.AttributeInfo;
import net.kapitencraft.lang.holder.bytecode.attributes.AttributeOwner;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantPoolEntry;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.field.RuntimeField;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.tool.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class RuntimeClass implements ScriptedClass, AttributeOwner {
    private final Map<String, Object> staticFields = new HashMap<>();
    public final ConstantPoolEntry[] constantPoolEntries;

    private final GeneratedMethodMap methods;

    private final MethodLookup lookup;

    private final Map<String, RuntimeField> allFields;

    private final String superclass;
    private final String[] implemented;
    private final String name;
    private final String packageRepresentation;
    private final Annotation[] annotations;

    private final short modifiers;
    private final Multimap<String, AttributeInfo> attributes;

    public RuntimeClass(ConstantPoolEntry[] constantPoolEntries, Map<String, DataMethodContainer> methods,
                        Map<String, RuntimeField> fields,
                        String superclass, String name, String packageRepresentation,
                        String[] implemented,
                        short modifiers, Annotation[] annotations,
                        AttributeInfo[] attributes
    ) {
        this.constantPoolEntries = constantPoolEntries;
        this.methods = new GeneratedMethodMap(methods);
        this.allFields = fields;
        this.superclass = superclass;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.implemented = implemented;
        this.modifiers = modifiers;
        this.attributes = AttributeOwner.createLookup(attributes);
        this.lookup = MethodLookup.createFromClass(this);
        this.annotations = annotations;
    }

    public <T extends ConstantPoolEntry> T getConstant(int idx) {
        return (T) constantPoolEntries[idx];
    }

    @Override
    public @NotNull ClassReference getFieldType(String name) {
        return Optional.ofNullable(getFields().get(name)).map(ScriptedField::type).orElse(ScriptedClass.super.getFieldType(name));
    }

    @Override
    public ScriptedClass getFieldDeclaring(String name) {
        return allFields.containsKey(name) ? this : ScriptedClass.super.getFieldDeclaring(name);
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return lookup.get(signature);
    }

    @Override
    public boolean hasMethod(String name) {
        return methods.has(name) || ScriptedClass.super.hasMethod(name);
    }

    @Override
    public Map<String, ? extends ScriptedField> getFields() {
        return Util.mergeMaps(ScriptedClass.super.getFields(), allFields);
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
    public boolean isInterface() {
        return false;
    }

    @Override
    public GeneratedMethodMap getMethods() {
        return methods;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return VarTypeManager.directParseType(superclass);
    }

    @Override
    public Object getStaticField(String name) {
        return staticFields.get(name);
    }

    @Override
    public Object setStaticField(String name, Object val) {
        return staticFields.put(name, val);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String pck() {
        return packageRepresentation;
    }

    @Override
    public String toString() { //jesus
        return "GeneratedClass{" + name + "}[" +
                "methods=" + methods.asMap() + ", " +
                "fields=" + allFields + ", " +
                "superclass=" + superclass + ']';
    }

    @Override
    public ClassReference[] interfaces() {
        return Arrays.stream(implemented).map(VarTypeManager::directParseType).toArray(ClassReference[]::new);
    }

    @Override
    public Annotation[] annotations() {
        return annotations;
    }

    @Override
    public short getModifiers() {
        return modifiers;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public <T extends AttributeInfo> Collection<T> getAttribute(String name) {
        return (Collection<T>) this.attributes.get(name);
    }

    @Override
    public boolean hasAttribute(String name) {
        return this.attributes.containsKey(name);
    }
}