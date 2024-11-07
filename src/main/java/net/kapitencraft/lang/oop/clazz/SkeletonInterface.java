package net.kapitencraft.lang.oop.clazz;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.oop.method.SkeletonMethod;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;

import java.util.List;
import java.util.Map;

public class SkeletonInterface implements LoxClass {
    private final String name;
    private final String pck;

    private final LoxClass superclass;

    private final LoxClass[] interfaces;

    private final Map<String, LoxClass> staticFields;

    private final Map<String, PreviewClass> enclosed;

    private final Map<String, DataMethodContainer> methods;
    private final MethodMap staticMethods;

    public SkeletonInterface(String name, String pck, LoxClass superclass, LoxClass[] interfaces, Map<String, LoxClass> staticFields, Map<String, PreviewClass> enclosed, Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods) {
        this.name = name;
        this.pck = pck;
        this.superclass = superclass;
        this.interfaces = interfaces;
        this.staticFields = staticFields;
        this.enclosed = enclosed;
        this.methods = methods;
        this.staticMethods = new MethodMap(staticMethods);
    }

    public static LoxClass fromCache(JsonObject data, String pck, PreviewClass[] enclosedClasses) {
        String name = GsonHelper.getAsString(data, "name");

        ImmutableMap<String, DataMethodContainer> methods = SkeletonMethod.readFromCache(data, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = SkeletonMethod.readFromCache(data, "staticMethods");

        ImmutableMap.Builder<String, LoxClass> staticFields = new ImmutableMap.Builder<>();
        {
            JsonObject fieldData = GsonHelper.getAsJsonObject(data, "staticFields");
            fieldData.asMap().forEach((s, element) ->
                    staticFields.put(s, ClassLoader.loadClassReference(element.getAsJsonObject(), "type"))
            );
        }

        return null;
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
    public String name() {
        return name;
    }

    @Override
    public String packageRepresentation() {
        return pck;
    }

    @Override
    public LoxClass superclass() {
        return superclass;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return staticFields.get(name);
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
        return methods.get(name).getMethodByOrdinal(ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        return methods.get(name).getMethodOrdinal(types);
    }

    @Override
    public boolean hasEnclosing(String name) {
        return enclosed.containsKey(name);
    }

    @Override
    public LoxClass getEnclosing(String name) {
        return enclosed.get(name);
    }
}
