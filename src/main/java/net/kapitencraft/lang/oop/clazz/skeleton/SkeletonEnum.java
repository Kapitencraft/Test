package net.kapitencraft.lang.oop.clazz.skeleton;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.EnumClass;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
import net.kapitencraft.lang.oop.clazz.inst.DynamicClassInstance;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.field.SkeletonField;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.SkeletonMethod;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.lang.tool.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SkeletonEnum implements EnumClass {
    private final String name;
    private final String pck;

    private final Map<String, SkeletonField> fields;
    private final Map<String, SkeletonField> staticFields;

    private final Map<String, ClassReference> enclosed;

    private final GeneratedMethodMap methods;

    public SkeletonEnum(String name, String pck,
                         Map<String, SkeletonField> staticFields, Map<String, SkeletonField> fields,
                         Map<String, ClassReference> enclosed,
                         Map<String, DataMethodContainer> methods) {
        this.name = name;
        this.pck = pck;
        this.staticFields = staticFields;
        this.fields = fields;
        this.enclosed = enclosed;
        this.methods = new GeneratedMethodMap(methods);
    }

    public static SkeletonEnum fromCache(JsonObject data, String pck, ClassReference[] enclosed) {
        String name = GsonHelper.getAsString(data, "name");

        ImmutableMap<String, DataMethodContainer> methods = SkeletonMethod.readFromCache(data, "methods");

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

        return new SkeletonEnum(name, pck,
                staticFields.build(), fields.build(),
                Arrays.stream(enclosed).collect(Collectors.toMap(ClassReference::name, Function.identity())),
                methods
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
    public ClassReference[] enclosed() {
        return enclosed.values().toArray(new ClassReference[0]);
    }

    @Override
    public Map<String, ? extends ScriptedField> enumConstants() {
        return Map.of();
    }

    @Override
    public void setConstantValues(Map<String, DynamicClassInstance> constants) {

    }

    @Override
    public DynamicClassInstance[] getConstants() {
        return new DynamicClassInstance[0];
    }

    @Override
    public Map<String, ? extends ScriptedField> staticFields() {
        return staticFields;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String pck() {
        return pck + "." + name;
    }

    @Override
    public boolean hasField(String name) {
        return this.fields.containsKey(name) || EnumClass.super.hasField(name);
    }

    @Override
    public ClassReference getFieldType(String name) {
        return Util.nonNullElse(fields.get(name).type(), EnumClass.super.getFieldType(name));
    }

    @Override
    public @Nullable ClassReference superclass() {
        return VarTypeManager.ENUM;
    }

    @Override
    public ClassReference getStaticFieldType(String name) {
        return staticFields.get(name).type();
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return Optional.ofNullable(methods.getMethod(signature)).orElseGet(() -> EnumClass.super.getMethod(signature));
    }

    @Override
    public short getModifiers() {
        return Modifiers.ENUM;
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
    public GeneratedMethodMap getMethods() {
        return methods;
    }

    @Override
    public RuntimeAnnotationClassInstance[] annotations() {
        return new RuntimeAnnotationClassInstance[0];
    }

}
