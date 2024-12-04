package net.kapitencraft.lang.oop.clazz;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.oop.field.SkeletonField;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.SkeletonMethod;
import net.kapitencraft.lang.run.algebra.OperationType;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SkeletonClass implements LoxClass {
    private final String name;
    private final String pck;

    private final LoxClass superclass;
    private final Map<String, SkeletonField> fields;
    private final Map<String, SkeletonField> staticFields;

    private final Map<String, PreviewClass> enclosed;

    private final MethodMap methods;
    private final Map<String, DataMethodContainer> staticMethods;
    private final ConstructorContainer constructor;

    private final boolean isAbstract, isFinal;

    public SkeletonClass(String name, String pck, LoxClass superclass,
                         Map<String, SkeletonField> staticFields, Map<String, SkeletonField> fields,
                         Map<String, PreviewClass> enclosed,
                         Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, ConstructorContainer.Builder constructor,
                         boolean isAbstract, boolean isFinal) {
        this.name = name;
        this.pck = pck;
        this.superclass = superclass;
        this.staticFields = staticFields;
        this.fields = fields;
        this.enclosed = enclosed;
        this.methods = new MethodMap(methods);
        this.staticMethods = staticMethods;
        this.constructor = constructor.build(this);
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
    }

    public SkeletonClass(String name, String pck, LoxClass superclass,
                         Map<String, SkeletonField> staticFields, Map<String, SkeletonField> fields,
                         Map<String, PreviewClass> enclosed,
                         Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, ConstructorContainer constructor,
                         boolean isAbstract, boolean isFinal) {
        this.name = name;
        this.pck = pck;
        this.superclass = superclass;
        this.staticFields = staticFields;
        this.fields = fields;
        this.enclosed = enclosed;
        this.methods = new MethodMap(methods);
        this.staticMethods = staticMethods;
        this.constructor = constructor;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
    }

    public static SkeletonClass fromCache(JsonObject data, String pck, PreviewClass[] enclosed) {
        String name = GsonHelper.getAsString(data, "name");
        LoxClass superclass = VarTypeManager.getClassForName(GsonHelper.getAsString(data, "superclass"));

        ImmutableMap<String, DataMethodContainer> methods = SkeletonMethod.readFromCache(data, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = SkeletonMethod.readFromCache(data, "staticMethods");

        ConstructorContainer constructorContainer = new ConstructorContainer(
                GsonHelper.getAsJsonArray(data, "constructors").asList()
                        .stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(SkeletonMethod::fromJson)
                        .toArray(SkeletonMethod[]::new)
        );
        ImmutableMap.Builder<String, SkeletonField> fields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(data, "fields");
            fieldData.asMap().forEach((s, element) -> {
                JsonObject object = element.getAsJsonObject();
                fields.put(s, new SkeletonField(ClassLoader.loadClassReference(object, "type"), object.has("isFinal") && GsonHelper.getAsBoolean(object, "isFinal")));
            });
        }
        ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(data, "staticFields");
            fieldData.asMap().forEach((s, element) -> {
                JsonObject object = element.getAsJsonObject();
                staticFields.put(s, new SkeletonField(ClassLoader.loadClassReference(object, "type"), object.has("isFinal") && GsonHelper.getAsBoolean(object, "isFinal")));
            });
        }

        List<String> flags = ClassLoader.readFlags(data);

        return new SkeletonClass(name, pck, superclass,
                staticFields.build(), fields.build(),
                Arrays.stream(enclosed).collect(Collectors.toMap(LoxClass::name, Function.identity())),
                methods, staticMethods,
                constructorContainer,
                flags.contains("isAbstract"), flags.contains("isFinal")
        );
    }

    @Override
    public Object getStaticField(String name) {
        throw new IllegalAccessError("cannot access field from skeleton");
    }

    @Override
    public Object assignStaticField(String name, Object val) {
        throw new IllegalAccessError("cannot access field from skeleton");
    }

    @Override
    public boolean hasInit() {
        return false;
    }

    @Override
    public void setInit() {

    }

    @Override
    public Map<String, ? extends LoxField> staticFields() {
        return staticFields;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String packageRepresentation() {
        return pck + "." + name;
    }

    @Override
    public boolean hasField(String name) {
        return this.fields.containsKey(name) || LoxClass.super.hasField(name);
    }

    @Override
    public LoxClass getFieldType(String name) {
        return Optional.ofNullable(this.fields.get(name)).map(SkeletonField::getType).orElseGet(() -> superclass.getFieldType(name));
    }

    @Override
    public LoxClass superclass() {
        return superclass;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return staticFields.get(name).getType();
    }

    @Override
    public ScriptedCallable getStaticMethod(String name, List<? extends LoxClass> args) {
        return staticMethods.get(name).getMethod(args);
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return staticMethods.get(name).getMethodByOrdinal(ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return staticMethods.get(name).getMethodOrdinal(args);
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.containsKey(name);
    }

    @Override
    public DataMethodContainer getConstructor() {
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
        return methods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        return methods.getMethodOrdinal(name, types);
    }

    @Override
    public boolean hasMethod(String name) {
        return methods.has(name) || LoxClass.super.hasMethod(name);
    }

    @Override
    public boolean hasEnclosing(String name) {
        return enclosed.containsKey(name);
    }

    @Override
    public LoxClass getEnclosing(String name) {
        return enclosed.get(name);
    }

    @Override
    public MethodMap getMethods() {
        return methods;
    }
}
