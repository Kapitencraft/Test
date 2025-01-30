package net.kapitencraft.lang.oop.clazz.generated;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.decl.AnnotationDecl;
import net.kapitencraft.lang.oop.clazz.AbstractAnnotationClass;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
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

    private final Map<String, ClassReference> enclosing;

    private final Map<String, AnnotationDecl.MethodWrapper> methods;
    private final List<String> abstracts;

    private final String name;
    private final String packageRepresentation;

    public GeneratedAnnotation(Map<String, ClassReference> enclosing, Map<String, AnnotationDecl.MethodWrapper> methods,
                               String name, String packageRepresentation) {
        this.methods = methods;
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

        return new GeneratedAnnotation(
                enclosedClasses, readMethods(data), name,
                pck);
    }

    private static Map<String, AnnotationDecl.MethodWrapper> readMethods(JsonObject object) {
        JsonObject sub = GsonHelper.getAsJsonObject(object, "methods");
        ImmutableMap.Builder<String, AnnotationDecl.MethodWrapper> builder = new ImmutableMap.Builder<>();
        sub.asMap().forEach((string, jsonElement) -> {
            JsonObject e = (JsonObject) jsonElement;
            builder.put(string, new AnnotationDecl.MethodWrapper(e.has("value") ? CacheLoader.readExpr(GsonHelper.getAsJsonObject(e, "value")) : null, ClassLoader.loadClassReference(e, "type")));
        });
        return builder.build();
    }

    private JsonObject addMethodData(CacheBuilder builder) {
        JsonObject methods = new JsonObject();
        this.methods.forEach((string, methodWrapper) -> {
            JsonObject method = new JsonObject();
            if (methodWrapper.val() != null) method.add("value", builder.cache(methodWrapper.val()));
            method.addProperty("type", methodWrapper.type().absoluteName());
            methods.add(string, method);
        });
        return methods;
    }

    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "annotation");
        object.addProperty("name", name);
        object.add("methods", this.addMethodData(cacheBuilder));

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
    public int getStaticMethodOrdinal(String name, List<ClassReference> args) {
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
    public Map<String, ScriptedField> getFields() {
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
    public int getMethodOrdinal(String name, List<ClassReference> types) {
        return types.isEmpty() && hasMethod(name) ? 0 : -1;
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

    @Override
    public List<String> getAbstracts() {
        return abstracts;
    }
}