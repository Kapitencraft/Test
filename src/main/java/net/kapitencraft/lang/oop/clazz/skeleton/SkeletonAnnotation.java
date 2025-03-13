package net.kapitencraft.lang.oop.clazz.skeleton;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.AbstractAnnotationClass;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.oop.method.annotation.SkeletonAnnotationMethod;
import net.kapitencraft.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.lang.oop.method.map.AnnotationMethodMap;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SkeletonAnnotation implements AbstractAnnotationClass {
    private final String name;
    private final String pck;

    private final Map<String, ClassReference> enclosed;

    private final AnnotationMethodMap methods;

    public SkeletonAnnotation(String name, String pck,
                              Map<String, ClassReference> enclosed, Map<String, AnnotationCallable> methods) {
        this.name = name;
        this.pck = pck;
        this.enclosed = enclosed;
        this.methods = new AnnotationMethodMap(methods);
    }

    public static SkeletonAnnotation fromCache(JsonObject data, String pck, ClassReference[] enclosed) {
        String name = GsonHelper.getAsString(data, "name");

        JsonObject methodData = GsonHelper.getAsJsonObject(data, "methods");

        ImmutableMap.Builder<String, AnnotationCallable> methods = new ImmutableMap.Builder<>();

        methodData.asMap().forEach((string, jsonElement) -> methods.put(string, SkeletonAnnotationMethod.fromJson((JsonObject) jsonElement)));

        return new SkeletonAnnotation(name, pck,
                Arrays.stream(enclosed).collect(Collectors.toMap(ClassReference::name, Function.identity())),
                methods.build()
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
    public ClassReference[] enclosed() {
        return enclosed.values().toArray(new ClassReference[0]);
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
        return pck;
    }

    @Override
    public boolean hasField(String name) {
        return false;
    }

    @Override
    public ClassReference getFieldType(String name) {
        return VarTypeManager.VOID.reference();
    }

    @Override
    public @Nullable ClassReference superclass() {
        return null;
    }

    @Override
    public ClassReference getStaticFieldType(String name) {
        return VarTypeManager.VOID.reference();
    }

    @Override
    public ScriptedCallable getStaticMethod(String name, ClassReference[] args) {
        return null;
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getStaticMethodOrdinal(String name, ClassReference[] args) {
        return -1;
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return false;
    }

    @Override
    public MethodContainer getConstructor() {
        return null;
    }

    @Override
    public short getModifiers() {
        return Modifiers.ANNOTATION;
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getMethodOrdinal(String name, ClassReference[] types) {
        return -1;
    }

    @Override
    public boolean hasMethod(String name) {
        return false;
    }

    @Override
    public boolean hasEnclosing(String name) {
        return enclosed.containsKey(name);
    }

    @Override
    public ClassReference getEnclosing(String name) {
        return enclosed.get(name);
    }

    @Override
    public AbstractMethodMap getMethods() {
        return methods;
    }

    @Override
    public AnnotationClassInstance[] annotations() {
        return new AnnotationClassInstance[0];
    }
}
