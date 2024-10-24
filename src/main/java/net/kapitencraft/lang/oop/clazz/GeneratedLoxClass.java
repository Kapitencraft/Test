package net.kapitencraft.lang.oop.clazz;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.tool.Util;

import java.util.*;

public final class GeneratedLoxClass implements LoxClass {
    private final Map<String, DataMethodContainer   > allMethods;
    private final Map<String, DataMethodContainer> allStaticMethods;

    private final Map<String, Object> staticFieldData = new HashMap<>();

    private final ConstructorContainer constructor;

    private final Map<String, GeneratedField> allFields;
    private final Map<String, GeneratedField> allStaticFields;

    private final Map<String, LoxClass> enclosing;

    private final LoxClass superclass;
    private final String name;
    private final String packageRepresentation;

    private final boolean isAbstract, isFinal;

    public GeneratedLoxClass(Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, ConstructorContainer.Builder constructor,
                             Map<String, GeneratedField> fields, Map<String, GeneratedField> staticFields,
                             Map<String, LoxClass> enclosing,
                             LoxClass superclass, String name, String packageRepresentation, boolean isAbstract, boolean isFinal) {
        this.allMethods = methods;
        this.allStaticMethods = staticMethods;
        this.constructor = constructor.build(this);
        this.allFields = fields;
        this.allStaticFields = staticFields;
        this.superclass = superclass;
        this.name = name;
        this.enclosing = enclosing;
        this.packageRepresentation = packageRepresentation;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
    }

    public GeneratedLoxClass(Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, List<LoxCallable> constructorData,
                             Map<String, GeneratedField> fields, Map<String, GeneratedField> staticFields,
                             LoxClass superclass, String name, String packageRepresentation,
                             Map<String, LoxClass> enclosing,
                             boolean isAbstract, boolean isFinal) {
        this.allMethods = methods;
        this.allStaticMethods = staticMethods;
        this.constructor = ConstructorContainer.fromCache(constructorData, this);
        this.allFields = fields;
        this.allStaticFields = staticFields;
        this.superclass = superclass;
        this.name = name;
        this.enclosing = enclosing;
        this.packageRepresentation = packageRepresentation;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
    }

    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("superclass", superclass.absoluteName());
        {
            JsonObject methods = new JsonObject();
            this.allMethods.forEach((name, container) -> methods.add(name, container.cache(cacheBuilder)));
            object.add("methods", methods);
        }
        {
            JsonObject staticMethods = new JsonObject();
            this.allStaticMethods.forEach((name, container) -> staticMethods.add(name, container.cache(cacheBuilder)));
            object.add("staticMethods", staticMethods);
        }
        object.add("constructor", constructor.cache(cacheBuilder));
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


    public Map<String, ? extends MethodContainer> getMethods() {
        return Util.mergeMaps(LoxClass.super.getMethods(), allMethods);
    }

    @Override
    public LoxClass getFieldType(String name) {
        return Optional.ofNullable(getFields().get(name)).map(LoxField::getType).orElse(LoxClass.super.getFieldType(name));
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return allStaticFields.get(name).getType();
    }

    @Override
    public boolean hasField(String name) {
        return allFields.containsKey(name) || LoxClass.super.hasField(name);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return allStaticMethods.get(name).getMethodOrdinal(args);
    }

    @Override
    public LoxCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return allStaticMethods.get(name).getMethodByOrdinal(ordinal);
    }

    @Override
    public LoxCallable getMethod(String name, List<LoxClass> args) {
        return Optional.ofNullable(allMethods.get(name)).map(container -> container.getMethod(args)).orElse(LoxClass.super.getMethod(name, args));
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return allStaticMethods.containsKey(name);
    }

    @Override
    public boolean hasMethod(String name) {
        return allMethods.containsKey(name) || LoxClass.super.hasMethod(name);
    }

    @Override
    public Map<String, LoxField> getFields() {
        return Util.mergeMaps(LoxClass.super.getFields(), allFields);
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
    public LoxCallable getMethodByOrdinal(String name, int ordinal) {
        return getMethods().get(name).getMethodByOrdinal(ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        return getMethods().get(name).getMethodOrdinal(types);
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
    public LoxClass superclass() {
        return superclass;
    }

    @Override
    public Object getStaticField(String name) {
        return staticFieldData.get(name);
    }

    @Override
    public Object assignStaticField(String name, Object val) {
        staticFieldData.put(name, val);
        return getStaticField(name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String packageRepresentation() {
        return packageRepresentation;
    }

    public GeneratedLoxClass[] enclosing() {
        return enclosing.values().toArray(new GeneratedLoxClass[0]);
    }

    @Override
    public String toString() { //jesus
        return "GeneratedLoxClass{" + name + "}[" +
                "methods=" + allMethods + ", " +
                "staticMethods=" + allStaticMethods + ", " +
                "fields=" + allFields + ", " +
                "staticFields=" + allStaticFields + ", " +
                "superclass=" + superclass + ']';
    }
}