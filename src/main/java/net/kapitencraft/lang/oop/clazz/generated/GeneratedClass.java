package net.kapitencraft.lang.oop.clazz.generated;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Util;

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

    private final Map<String, LoxClass> enclosing;

    private final LoxClass superclass;
    private final LoxClass[] implemented;
    private final String name;
    private final String packageRepresentation;

    private final boolean isAbstract, isFinal;

    public GeneratedClass(Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, ConstructorContainer.Builder constructor,
                          Map<String, GeneratedField> fields, Map<String, GeneratedField> staticFields,
                          Map<String, LoxClass> enclosing,
                          LoxClass superclass, LoxClass[] implemented, String name, String packageRepresentation, boolean isAbstract, boolean isFinal) {
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
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public GeneratedClass(Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, List<ScriptedCallable> constructorData,
                          Map<String, GeneratedField> fields, Map<String, GeneratedField> staticFields,
                          LoxClass superclass, String name, String packageRepresentation,
                          Map<String, LoxClass> enclosing, LoxClass[] implemented,
                          boolean isAbstract, boolean isFinal) {
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
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public static LoxClass load(JsonObject data, List<LoxClass> enclosed, String pck) {
        String name = GsonHelper.getAsString(data, "name");
        LoxClass superclass = ClassLoader.loadClassReference(data, "superclass");
        LoxClass[] implemented = GsonHelper.getAsJsonArray(data, "interfaces").asList().stream().map(JsonElement::getAsString).map(VarTypeManager::getClassForName).toArray(LoxClass[]::new);

        if (superclass == null) throw new IllegalArgumentException(String.format("could not find parent class for class '%s': '%s'", name, GsonHelper.getAsString(data, "superclass")));
        ImmutableMap<String, DataMethodContainer> methods = DataMethodContainer.load(data, name, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = DataMethodContainer.load(data, name, "staticMethods");

        List<ScriptedCallable> constructorData = new ArrayList<>();
        GsonHelper.getAsJsonArray(data, "constructors").asList().stream().map(JsonElement::getAsJsonObject).map(GeneratedCallable::load).forEach(constructorData::add);

        ImmutableMap<String, GeneratedField> fields = GeneratedField.loadFieldMap(data, "fields");
        ImmutableMap<String, GeneratedField> staticFields = GeneratedField.loadFieldMap(data, "staticFields");

        List<String> flags = GsonHelper.getAsJsonArray(data, "flags").asList().stream().map(JsonElement::getAsString).toList();

        Map<String, LoxClass> enclosedClasses = enclosed.stream().collect(Collectors.toMap(LoxClass::name, Function.identity()));

        return new GeneratedClass(
                methods, staticMethods, constructorData,
                fields, staticFields,
                superclass,
                name, pck,
                enclosedClasses,
                implemented,
                flags.contains("isAbstract"), flags.contains("isFinal")
        );
    }

    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "class");
        object.addProperty("name", name);
        object.addProperty("superclass", superclass.absoluteName());
        {
            JsonArray parentInterfaces = new JsonArray();
            Arrays.stream(this.implemented).map(LoxClass::absoluteName).forEach(parentInterfaces::add);
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


        {
            JsonArray flags = new JsonArray();
            if (isAbstract) flags.add("isAbstract");
            if (isFinal) flags.add("isFinal");
            object.add("flags", flags);
        }

        return object;
    }

    @Override
    public LoxClass getFieldType(String name) {
        return Optional.ofNullable(getFields().get(name)).map(LoxField::getType).orElse(CacheableClass.super.getFieldType(name));
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return allStaticFields.get(name).getType();
    }

    @Override
    public boolean hasField(String name) {
        return allFields.containsKey(name) || CacheableClass.super.hasField(name);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return staticMethods.getMethodOrdinal(name, args);
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return staticMethods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public ScriptedCallable getMethod(String name, List<LoxClass> args) {
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
    public Map<String, LoxField> getFields() {
        return Util.mergeMaps(CacheableClass.super.getFields(), allFields);
    }

    @Override
    public MethodContainer getConstructor() {
        return constructor;
    }
    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
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
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        return lookup.getMethodOrdinal(name, types);
    }

    @Override
    public boolean hasEnclosing(String name) {
        return false;
    }

    @Override
    public LoxClass getEnclosing(String name) {
        return null;
    }

    @Override
    public GeneratedMethodMap getMethods() {
        return methods;
    }

    @Override
    public LoxClass superclass() {
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
    public Map<String, ? extends LoxField> staticFields() {
        return allStaticFields;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String packageRepresentation() {
        return packageRepresentation;
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    public CacheableClass[] enclosed() {
        return enclosing.values().toArray(new CacheableClass[0]);
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
    public LoxClass[] interfaces() {
        return implemented;
    }
}