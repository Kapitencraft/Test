package net.kapitencraft.lang.oop.clazz.generated;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.AbstractAnnotationClass;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.map.AnnotationMethodMap;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GeneratedAnnotation implements CacheableClass, AbstractAnnotationClass {
    //TODO extract generated to single class

    private final Map<String, ClassReference> enclosing;

    private final Map<String, Holder.Class.MethodWrapper> methods;
    private final List<String> abstracts;

    private final String name;
    private final String packageRepresentation;
    private final AnnotationClassInstance[] annotations;

    public GeneratedAnnotation(Map<String, ClassReference> enclosing, Map<String, Holder.Class.MethodWrapper> methods,
                               String name, String packageRepresentation, AnnotationClassInstance[] annotations) {
        this.methods = methods;
        this.annotations = annotations;
        List<String> abstracts = new ArrayList<>();
        methods.forEach((string, methodWrapper) -> {
            if (methodWrapper.isAbstract()) abstracts.add(string);
        });
        this.abstracts = abstracts;
        this.name = name;
        this.enclosing = enclosing;
        this.packageRepresentation = packageRepresentation;
    }

    public static ScriptedClass load(JsonObject data, List<ClassReference> enclosed, String pck) {
        String name = GsonHelper.getAsString(data, "name");

        Map<String, ClassReference> enclosedClasses = enclosed.stream().collect(Collectors.toMap(ClassReference::name, Function.identity()));

        AnnotationClassInstance[] annotations = CacheLoader.readAnnotations(data);

        return new GeneratedAnnotation(
                enclosedClasses, readMethods(data), name,
                pck, annotations);
    }

    private static Map<String, Holder.Class.MethodWrapper> readMethods(JsonObject object) {
        JsonObject sub = GsonHelper.getAsJsonObject(object, "methods");
        ImmutableMap.Builder<String, Holder.Class.MethodWrapper> builder = new ImmutableMap.Builder<>();
        sub.asMap().forEach((string, jsonElement) -> {
            JsonObject e = (JsonObject) jsonElement;
            AnnotationClassInstance[] annotations = CacheLoader.readAnnotations(e);
            builder.put(string, new Holder.Class.MethodWrapper(e.has("value") ? CacheLoader.readExpr(GsonHelper.getAsJsonObject(e, "value")) : null, ClassLoader.loadClassReference(e, "type"), annotations));
        });
        return builder.build();
    }

    private JsonObject addMethodData(CacheBuilder builder) {
        JsonObject methods = new JsonObject();
        this.methods.forEach((string, methodWrapper) -> {
            JsonObject method = new JsonObject();
            if (methodWrapper.val() != null) method.add("value", builder.cache(methodWrapper.val()));
            method.addProperty("type", methodWrapper.type().absoluteName());
            method.add("annotations", builder.cacheAnnotations(methodWrapper.annotations()));
            methods.add(string, method);
        });
        return methods;
    }

    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "annotation");
        object.addProperty("name", name);
        object.add("methods", this.addMethodData(cacheBuilder));
        object.add("annotations", cacheBuilder.cacheAnnotations(this.annotations));

        return object;
    }

    @Override
    public ClassReference getFieldType(String name) {
        return Optional.ofNullable(getFields().get(name)).map(ScriptedField::getType).orElse(CacheableClass.super.getFieldType(name));
    }

    @Override
    public ClassReference getStaticFieldType(String name) {
        return VarTypeManager.VOID.reference();
    }

    @Override
    public boolean hasField(String name) {
        return false;
    }

    @Override
    public int getStaticMethodOrdinal(String name, ClassReference[] args) {
        return -1;
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return false;
    }

    @Override
    public boolean hasMethod(String name) {
        return methods.containsKey(name);
    }

    @Override
    public Map<String, ? extends ScriptedField> getFields() {
        return Map.of();
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
        return methods.get(name);
    }

    @Override
    public int getMethodOrdinal(String name, ClassReference[] types) {
        return types.length == 0 && hasMethod(name) ? 0 : -1;
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
    public AnnotationMethodMap getMethods() {
        return null;
    }

    @Override
    public AnnotationClassInstance[] annotations() {
        return annotations;
    }

    @Override
    public short getModifiers() {
        return Modifiers.ANNOTATION;
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
    public ClassReference[] interfaces() {
        return new ClassReference[0];
    }

    @Override
    public Map<String, ? extends ScriptedField> staticFields() {
        return Map.of();
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
    public @Nullable ClassReference superclass() {
        return null;
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    public ClassReference[] enclosed() {
        return enclosing.values().toArray(new ClassReference[0]);
    }

    @Override
    public MethodLookup methods() {
        return null;
    }

    @Override
    public String toString() {
        return "GeneratedAnnotation{" + name + "}";
    }
}