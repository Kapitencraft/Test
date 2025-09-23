package net.kapitencraft.lang.oop.clazz.generated;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.field.RuntimeField;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.lang.tool.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class RuntimeClass implements ScriptedClass {
    private final GeneratedMethodMap methods;

    private final MethodLookup lookup;

    private final Map<String, RuntimeField> allFields;
    private final Map<String, RuntimeField> allStaticFields;

    private final ClassReference superclass;
    private final ClassReference[] implemented;
    private final String name;
    private final String packageRepresentation;

    private final short modifiers;

    private final RuntimeAnnotationClassInstance[] annotations;

    public RuntimeClass(Map<String, DataMethodContainer> methods,
                        Map<String, RuntimeField> fields, Map<String, RuntimeField> staticFields,
                        ClassReference superclass, String name, String packageRepresentation,
                        ClassReference[] implemented,
                        short modifiers, RuntimeAnnotationClassInstance[] annotations) {
        this.methods = new GeneratedMethodMap(methods);
        this.allFields = fields;
        this.allStaticFields = staticFields;
        this.superclass = superclass;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.implemented = implemented;
        this.modifiers = modifiers;
        this.annotations = annotations;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public static ScriptedClass load(JsonObject data, String pck) {
        String name = GsonHelper.getAsString(data, "name");
        ClassReference superclass = ClassLoader.loadClassReference(data, "superclass");
        ClassReference[] implemented = ClassLoader.loadInterfaces(data);

        if (superclass == null) throw new IllegalArgumentException(String.format("could not find target class for class '%s': '%s'", name, GsonHelper.getAsString(data, "superclass")));
        ImmutableMap<String, DataMethodContainer> methods = DataMethodContainer.load(data, name, "methods");

        ImmutableMap<String, RuntimeField> fields = RuntimeField.loadFieldMap(data, "fields");
        ImmutableMap<String, RuntimeField> staticFields = RuntimeField.loadFieldMap(data, "staticFields");

        short modifiers = data.has("modifiers") ? GsonHelper.getAsShort(data, "modifiers") : 0;

        RuntimeAnnotationClassInstance[] annotations = CacheLoader.readAnnotations(data);

        return new RuntimeClass(
                methods,
                fields, staticFields,
                superclass,
                name, pck,
                implemented,
                modifiers,
                annotations
        );
    }

    @Override
    public ClassReference getFieldType(String name) {
        return Optional.ofNullable(getFields().get(name)).map(ScriptedField::type).orElse(ScriptedClass.super.getFieldType(name));
    }

    @Override
    public ClassReference getStaticFieldType(String name) {
        return allStaticFields.get(name).type();
    }

    @Override
    public boolean hasField(String name) {
        return allFields.containsKey(name) || ScriptedClass.super.hasField(name);
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
        return superclass;
    }

    @Override
    public Map<String, ? extends ScriptedField> staticFields() {
        return allStaticFields;
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
                "staticFields=" + allStaticFields + ", " +
                "superclass=" + superclass + ']';
    }

    @Override
    public ClassReference[] interfaces() {
        return implemented;
    }

    @Override
    public RuntimeAnnotationClassInstance[] annotations() {
        return annotations;
    }

    @Override
    public short getModifiers() {
        return modifiers;
    }
}