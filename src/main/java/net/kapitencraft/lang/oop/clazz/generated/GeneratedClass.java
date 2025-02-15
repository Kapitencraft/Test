package net.kapitencraft.lang.oop.clazz.generated;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GeneratedClass implements CacheableClass {
    private final GeneratedMethodMap methods;
    private final GeneratedMethodMap staticMethods;
    private final Map<String, DataMethodContainer> allMethods;

    private final MethodLookup lookup;

    private final ConstructorContainer constructor;

    private final Map<String, GeneratedField> allFields;
    private final Map<String, GeneratedField> allStaticFields;

    private final Map<String, ClassReference> enclosing;

    private final ClassReference superclass;
    private final ClassReference[] implemented;
    private final String name;
    private final String packageRepresentation;

    private final short modifiers;

    private final AnnotationClassInstance[] annotations;

    public GeneratedClass(Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, ConstructorContainer.Builder constructor,
                          Map<String, GeneratedField> fields, Map<String, GeneratedField> staticFields,
                          Map<String, ClassReference> enclosing,
                          ClassReference superclass, ClassReference[] implemented, String name, String packageRepresentation, short modifiers, AnnotationClassInstance[] annotations) {
        this.methods = new GeneratedMethodMap(methods);
        this.allMethods = methods;
        this.staticMethods = new GeneratedMethodMap(staticMethods);
        this.constructor = constructor.build(this);
        this.allFields = fields;
        this.allStaticFields = staticFields;
        this.superclass = superclass;
        this.implemented = implemented;
        this.name = name;
        this.enclosing = enclosing;
        this.packageRepresentation = packageRepresentation;
        this.modifiers = modifiers;
        this.annotations = annotations;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public GeneratedClass(Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, List<ScriptedCallable> constructorData,
                          Map<String, GeneratedField> fields, Map<String, GeneratedField> staticFields,
                          ClassReference superclass, String name, String packageRepresentation,
                          Map<String, ClassReference> enclosing, ClassReference[] implemented,
                          short modifiers, AnnotationClassInstance[] annotations) {
        this.methods = new GeneratedMethodMap(methods);
        this.allMethods = methods;
        this.staticMethods = new GeneratedMethodMap(staticMethods);
        this.constructor = ConstructorContainer.fromCache(constructorData, this);
        this.allFields = fields;
        this.allStaticFields = staticFields;
        this.superclass = superclass;
        this.name = name;
        this.enclosing = enclosing;
        this.packageRepresentation = packageRepresentation;
        this.implemented = implemented;
        this.modifiers = modifiers;
        this.annotations = annotations;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public static ScriptedClass load(JsonObject data, List<ClassReference> enclosed, String pck) {
        String name = GsonHelper.getAsString(data, "name");
        ClassReference superclass = ClassLoader.loadClassReference(data, "superclass");
        ClassReference[] implemented = ClassLoader.loadInterfaces(data);

        if (superclass == null) throw new IllegalArgumentException(String.format("could not find target class for class '%s': '%s'", name, GsonHelper.getAsString(data, "superclass")));
        ImmutableMap<String, DataMethodContainer> methods = DataMethodContainer.load(data, name, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = DataMethodContainer.load(data, name, "staticMethods");

        List<ScriptedCallable> constructorData = new ArrayList<>();
        GsonHelper.getAsJsonArray(data, "constructors").asList().stream().map(JsonElement::getAsJsonObject).map(GeneratedCallable::load).forEach(constructorData::add);

        ImmutableMap<String, GeneratedField> fields = GeneratedField.loadFieldMap(data, "fields");
        ImmutableMap<String, GeneratedField> staticFields = GeneratedField.loadFieldMap(data, "staticFields");

        short modifiers = data.has("modifiers") ? GsonHelper.getAsShort(data, "modifiers") : 0;

        AnnotationClassInstance[] annotations = CacheLoader.readAnnotations(data);

        Map<String, ClassReference> enclosedClasses = enclosed.stream().collect(Collectors.toMap(ClassReference::name, Function.identity()));

        return new GeneratedClass(
                methods, staticMethods, constructorData,
                fields, staticFields,
                superclass,
                name, pck,
                enclosedClasses,
                implemented,
                modifiers,
                annotations
        );
    }

    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "class");
        object.addProperty("name", name);
        object.addProperty("superclass", superclass.absoluteName());
        {
            JsonArray parentInterfaces = new JsonArray();
            Arrays.stream(this.implemented).map(ClassReference::absoluteName).forEach(parentInterfaces::add);
            object.add("interfaces", parentInterfaces);
        }
        object.add("methods", methods.save(cacheBuilder));
        object.add("staticMethods", staticMethods.save(cacheBuilder));
        object.add("constructors", constructor.cache(cacheBuilder));
        {
            JsonObject fields = new JsonObject();
            allFields.forEach((name, field) -> fields.add(name, field.cache(cacheBuilder)));
            object.add("fields", fields);
        }
        {
            JsonObject staticFields = new JsonObject();
            allStaticFields.forEach((name, field) -> staticFields.add(name, field.cache(cacheBuilder)));
            object.add("staticFields", staticFields);
        }

        object.add("annotations", cacheBuilder.cacheAnnotations(this.annotations));

        if (this.modifiers != 0) object.addProperty("modifiers", modifiers);

        return object;
    }

    @Override
    public ClassReference getFieldType(String name) {
        return Optional.ofNullable(getFields().get(name)).map(ScriptedField::getType).orElse(CacheableClass.super.getFieldType(name));
    }

    @Override
    public ClassReference getStaticFieldType(String name) {
        return allStaticFields.get(name).getType();
    }

    @Override
    public boolean hasField(String name) {
        return allFields.containsKey(name) || CacheableClass.super.hasField(name);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<ClassReference> args) {
        return staticMethods.getMethodOrdinal(name, args);
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return staticMethods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public ScriptedCallable getMethod(String name, List<ClassReference> args) {
        return Optional.ofNullable(allMethods.get(name)).map(container -> container.getMethod(args)).orElse(CacheableClass.super.getMethod(name, args));
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.has(name);
    }

    @Override
    public boolean hasMethod(String name) {
        return allMethods.containsKey(name) || CacheableClass.super.hasMethod(name);
    }

    @Override
    public Map<String, ScriptedField> getFields() {
        return Util.mergeMaps(CacheableClass.super.getFields(), allFields);
    }

    @Override
    public MethodContainer getConstructor() {
        return constructor;
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
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return lookup.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, List<ClassReference> types) {
        return lookup.getMethodOrdinal(name, types);
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
    public GeneratedMethodMap getMethods() {
        return methods;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return superclass;
    }

    boolean init = false;

    @Override
    public boolean hasInit() {
        return init;
    }

    @Override
    public void setInit() {
        init = true;
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

    public ClassReference[] enclosed() {
        return enclosing.values().toArray(new ClassReference[0]);
    }

    @Override
    public MethodLookup methods() {
        return lookup;
    }

    @Override
    public String toString() { //jesus
        return "GeneratedClass{" + name + "}[" +
                "methods=" + allMethods + ", " +
                "staticMethods=" + staticMethods + ", " +
                "fields=" + allFields + ", " +
                "staticFields=" + allStaticFields + ", " +
                "superclass=" + superclass + ']';
    }

    @Override
    public ClassReference[] interfaces() {
        return implemented;
    }

    @Override
    public ClassType getClassType() {
        return ClassType.CLASS;
    }

    @Override
    public AnnotationClassInstance[] annotations() {
        return annotations;
    }
}