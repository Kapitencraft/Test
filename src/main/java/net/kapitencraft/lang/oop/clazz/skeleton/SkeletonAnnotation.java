package net.kapitencraft.lang.oop.clazz.skeleton;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.oop.field.SkeletonField;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.oop.method.SkeletonMethod;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SkeletonAnnotation implements LoxClass {
    private final String name;
    private final String pck;

    private final Map<String, PreviewClass> enclosed;

    public SkeletonAnnotation(String name, String pck,
                              Map<String, PreviewClass> enclosed) {
        this.name = name;
        this.pck = pck;
        this.enclosed = enclosed;
    }

    public static SkeletonAnnotation fromCache(JsonObject data, String pck, PreviewClass[] enclosed) {
        String name = GsonHelper.getAsString(data, "name");

        ImmutableMap<String, DataMethodContainer> methods = SkeletonMethod.readFromCache(data, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = SkeletonMethod.readFromCache(data, "staticMethods");

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

        return new SkeletonAnnotation(name, pck,
                Arrays.stream(enclosed).collect(Collectors.toMap(LoxClass::name, Function.identity()))
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
        return Map.of();
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
        return false;
    }

    @Override
    public LoxClass getFieldType(String name) {
        return VarTypeManager.VOID;
    }

    @Override
    public LoxClass superclass() {
        return null;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return VarTypeManager.VOID;
    }

    @Override
    public ScriptedCallable getStaticMethod(String name, List<? extends LoxClass> args) {
        return null;
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return null;
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
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
        return null;
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
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
    public LoxClass getEnclosing(String name) {
        return enclosed.get(name);
    }

    @Override
    public MethodMap getMethods() {
        return null;
    }
}
