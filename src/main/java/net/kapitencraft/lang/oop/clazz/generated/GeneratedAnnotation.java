package net.kapitencraft.lang.oop.clazz.generated;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GeneratedAnnotation implements CacheableClass {
    private final MethodMap methods;
    private final MethodMap staticMethods;
    private final Map<String, DataMethodContainer> allMethods;

    private final MethodLookup lookup;

    private final Map<String, GeneratedField> allFields;
    private final Map<String, GeneratedField> allStaticFields;

    private final Map<String, LoxClass> enclosing;

    private final String name;
    private final String packageRepresentation;

    public GeneratedAnnotation(Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods,
                               Map<String, GeneratedField> fields, Map<String, GeneratedField> staticFields,
                               Map<String, LoxClass> enclosing,
                               String name, String packageRepresentation) {
        this.methods = new MethodMap(methods);
        this.allMethods = methods;
        this.staticMethods = new MethodMap(staticMethods);
        this.allFields = fields;
        this.allStaticFields = staticFields;
        this.name = name;
        this.enclosing = enclosing;
        this.packageRepresentation = packageRepresentation;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public static LoxClass load(JsonObject data, List<LoxClass> enclosed, String pck) {
        String name = GsonHelper.getAsString(data, "name");

        ImmutableMap<String, DataMethodContainer> methods = DataMethodContainer.load(data, name, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = DataMethodContainer.load(data, name, "staticMethods");

        ImmutableMap<String, GeneratedField> fields = GeneratedField.loadFieldMap(data, "fields");
        ImmutableMap<String, GeneratedField> staticFields = GeneratedField.loadFieldMap(data, "staticFields");

        Map<String, LoxClass> enclosedClasses = enclosed.stream().collect(Collectors.toMap(LoxClass::name, Function.identity()));

        return new GeneratedAnnotation(
                methods, staticMethods,
                fields, staticFields,
                enclosedClasses, name, pck
        );
    }

    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "class");
        object.addProperty("name", name);
        object.add("methods", methods.save(cacheBuilder));
        object.add("staticMethods", staticMethods.save(cacheBuilder));
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
        return null;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return true;
    }

    @Override
    public boolean isInterface() {
        return true;
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
    public MethodMap getMethods() {
        return methods;
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
    public void clInit() {
        if (Interpreter.suppressClassLoad) return;
        Interpreter.INSTANCE.pushCallIndex(-1);
        Interpreter.INSTANCE.pushCall(this.absoluteName(), "<clinit>", this.name());
        CacheableClass.super.clInit();
        this.enclosing.values().forEach(LoxClass::clInit);
        Interpreter.INSTANCE.popCall();
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

    @Override
    public LoxClass superclass() {
        return null;
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    public CacheableClass[] enclosing() {
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
                "staticFields=" + allStaticFields + ']';
    }
}