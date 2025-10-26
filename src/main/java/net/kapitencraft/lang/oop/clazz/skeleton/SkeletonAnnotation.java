package net.kapitencraft.lang.oop.clazz.skeleton;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.AbstractAnnotationClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.annotation.AnnotationCallable;
import net.kapitencraft.lang.oop.method.annotation.SkeletonAnnotationMethod;
import net.kapitencraft.lang.oop.method.map.AbstractMethodMap;
import net.kapitencraft.lang.oop.method.map.AnnotationMethodMap;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SkeletonAnnotation implements AbstractAnnotationClass {
    private final String name;
    private final String pck;

    private final AnnotationMethodMap methods;

    public SkeletonAnnotation(String name, String pck, Map<String, AnnotationCallable> methods) {
        this.name = name;
        this.pck = pck;
        this.methods = new AnnotationMethodMap(methods);
    }

    public static SkeletonAnnotation fromCache(JsonObject data, String pck) {
        String name = GsonHelper.getAsString(data, "name");

        JsonObject methodData = GsonHelper.getAsJsonObject(data, "methods");

        ImmutableMap.Builder<String, AnnotationCallable> methods = new ImmutableMap.Builder<>();

        methodData.asMap().forEach((string, jsonElement) -> methods.put(string, SkeletonAnnotationMethod.fromJson((JsonObject) jsonElement)));

        return new SkeletonAnnotation(name, pck,
                methods.build()
        );
    }

    @Override
    public Object getStaticField(String name) {
        throw new IllegalAccessError("cannot access field from skeleton");
    }

    @Override
    public Object setStaticField(String name, Object val) {
        throw new IllegalAccessError("cannot access field from skeleton");
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
    public ScriptedCallable getMethod(String signature) {
        return methods.getMethod(signature);
    }

    @Override
    public short getModifiers() {
        return Modifiers.ANNOTATION;
    }

    @Override
    public boolean hasMethod(String name) {
        return false;
    }

    @Override
    public AbstractMethodMap getMethods() {
        return methods;
    }

    @Override
    public RuntimeAnnotationClassInstance[] annotations() {
        return new RuntimeAnnotationClassInstance[0];
    }

    @Override
    public boolean isNative() {
        return false;
    }
}
