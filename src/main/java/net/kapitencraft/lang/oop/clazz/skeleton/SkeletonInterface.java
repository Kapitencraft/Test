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
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SkeletonInterface implements LoxClass {
    private final String name;
    private final String pck;

    private final LoxClass[] interfaces;

    private final Map<String, SkeletonField> staticFields;

    private final Map<String, PreviewClass> enclosed;

    private final MethodMap methods;
    private final MethodMap staticMethods;

    public SkeletonInterface(String name, String pck, LoxClass[] interfaces, Map<String, SkeletonField> staticFields, Map<String, PreviewClass> enclosed, Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods) {
        this.name = name;
        this.pck = pck;
        this.interfaces = interfaces;
        this.staticFields = staticFields;
        this.enclosed = enclosed;
        this.methods = new MethodMap(methods);
        this.staticMethods = new MethodMap(staticMethods);
    }

    public static LoxClass fromCache(JsonObject data, String pck, PreviewClass[] enclosed) {
        String name = GsonHelper.getAsString(data, "name");

        ImmutableMap<String, DataMethodContainer> methods = SkeletonMethod.readFromCache(data, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = SkeletonMethod.readFromCache(data, "staticMethods");

        ImmutableMap.Builder<String, SkeletonField> staticFields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(data, "staticFields");
            fieldData.asMap().forEach((s, element) -> {
                JsonObject object = element.getAsJsonObject();
                staticFields.put(s, new SkeletonField(ClassLoader.loadClassReference(object, "type"), object.has("isFinal") && GsonHelper.getAsBoolean(object, "isFinal")));
            });
        }

        LoxClass[] interfaces = GsonHelper.getAsJsonArray(data, "interfaces").asList().stream().map(JsonElement::getAsString).map(VarTypeManager::getClassForName).toArray(LoxClass[]::new);

        return new SkeletonInterface(
                name,
                pck,
                interfaces,
                staticFields.build(),
                Arrays.stream(enclosed).collect(Collectors.toMap(LoxClass::name, Function.identity())),
                methods,
                staticMethods
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
        return pck;
    }

    @Override
    public LoxClass superclass() {
        return null;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return staticFields.get(name).getType();
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return staticMethods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return staticMethods.getMethodOrdinal(name, args);
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.has(name);
    }

    @Override
    public MethodContainer getConstructor() {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return true;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isInterface() {
        return true;
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
