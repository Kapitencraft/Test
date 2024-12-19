package net.kapitencraft.lang.oop.clazz.generated;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.GsonHelper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GeneratedAnnotation implements CacheableClass {
    private final MethodLookup lookup;

    private final Map<String, LoxClass> enclosing;

    private final String name;
    private final String packageRepresentation;

    public GeneratedAnnotation(Map<String, LoxClass> enclosing,
                               String name, String packageRepresentation) {
        this.name = name;
        this.enclosing = enclosing;
        this.packageRepresentation = packageRepresentation;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public static LoxClass load(JsonObject data, List<LoxClass> enclosed, String pck) {
        String name = GsonHelper.getAsString(data, "name");

        Map<String, LoxClass> enclosedClasses = enclosed.stream().collect(Collectors.toMap(LoxClass::name, Function.identity()));

        return new GeneratedAnnotation(
                enclosedClasses, name, pck
        );
    }

    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "class");
        object.addProperty("name", name);

        return object;
    }

    @Override
    public LoxClass getFieldType(String name) {
        return Optional.ofNullable(getFields().get(name)).map(LoxField::getType).orElse(CacheableClass.super.getFieldType(name));
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return VarTypeManager.VOID;
    }

    @Override
    public boolean hasField(String name) {
        return false;
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return -1;
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public ScriptedCallable getMethod(String name, List<LoxClass> args) {
        return null;
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return false;
    }

    @Override
    public boolean hasMethod(String name) {
        return false;
    }

    @Override
    public Map<String, LoxField> getFields() {
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
        return Map.of();
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
    public String toString() {
        return "GeneratedAnnotation{" + name + "}";
    }
}