package net.kapitencraft.lang.oop.clazz.skeleton;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.field.SkeletonField;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.SkeletonMethod;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SkeletonInterface implements ScriptedClass {
    private final String name;
    private final String pck;

    private final ClassReference[] interfaces;

    private final Map<String, SkeletonField> staticFields;

    private final Holder.Generics generics;
    private final Map<String, ClassReference> enclosed;

    private final GeneratedMethodMap methods;

    public SkeletonInterface(String name, String pck, ClassReference[] interfaces, Map<String, SkeletonField> staticFields, Holder.Generics generics, Map<String, ClassReference> enclosed, Map<String, DataMethodContainer> methods) {
        this.name = name;
        this.pck = pck;
        this.interfaces = interfaces;
        this.staticFields = staticFields;
        this.generics = generics;
        this.enclosed = enclosed;
        this.methods = new GeneratedMethodMap(methods);
    }

    public static ScriptedClass fromCache(JsonObject data, String pck, ClassReference[] enclosed) {
        String name = GsonHelper.getAsString(data, "name");

        ImmutableMap<String, DataMethodContainer> methods = SkeletonMethod.readFromCache(data, "methods");

        ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(data, "staticFields");
            fieldData.asMap().forEach((s, element) -> {
                JsonObject object = element.getAsJsonObject();
                staticFields.put(s, new SkeletonField(ClassLoader.loadClassReference(object, "type"), object.has("isFinal") && GsonHelper.getAsBoolean(object, "isFinal")));
            });
        }

        ClassReference[] interfaces = GsonHelper.getAsJsonArray(data, "interfaces").asList().stream().map(JsonElement::getAsString).map(VarTypeManager::getClassForName).toArray(ClassReference[]::new);

        return new SkeletonInterface(
                name,
                pck,
                interfaces,
                staticFields.build(),
                null,
                Arrays.stream(enclosed).collect(Collectors.toMap(ClassReference::name, Function.identity())),
                methods
        );
    }

    @Override
    public Object getStaticField(String name) {
        return null;
    }

    @Override
    public Object assignStaticField(String name, Object val) {
        return null;
    }

    @Override
    public @Nullable Holder.Generics getGenerics() {
        return generics;
    }

    @Override
    public ClassReference[] enclosed() {
        return enclosed.values().toArray(new ClassReference[0]);
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
        return pck;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return null;
    }

    @Override
    public ClassReference getStaticFieldType(String name) {
        return staticFields.get(name).type();
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return methods.getMethod(signature);
    }

    @Override
    public short getModifiers() {
        return Modifiers.INTERFACE;
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
    public ClassReference[] interfaces() {
        return interfaces;
    }

    @Override
    public RuntimeAnnotationClassInstance[] annotations() {
        return new RuntimeAnnotationClassInstance[0];
    }

    @Override
    public boolean hasMethod(String name) {
        return methods.has(name);
    }
}
