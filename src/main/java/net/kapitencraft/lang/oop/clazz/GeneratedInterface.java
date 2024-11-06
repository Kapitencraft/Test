package net.kapitencraft.lang.oop.clazz;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.GsonHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GeneratedInterface implements LoxInterface, CacheableClass {
    private final Map<String, DataMethodContainer> allMethods;
    private final MethodMap staticMethods;
    private final Map<String, GeneratedField> allStaticFields;

    private final LoxClass[] parentInterfaces;

    private final Map<String, LoxClass> enclosing;

    private final String name;
    private final String packageRepresentation;

    public GeneratedInterface(Map<String, DataMethodContainer> allMethods, Map<String, DataMethodContainer> allStaticMethods, Map<String, GeneratedField> allStaticFields, LoxClass[] parentInterfaces, Map<String, LoxClass> enclosing, String name, String packageRepresentation) {
        this.allMethods = allMethods;
        this.staticMethods = new MethodMap(allStaticMethods);
        this.allStaticFields = allStaticFields;
        this.parentInterfaces = parentInterfaces;
        this.enclosing = enclosing;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
    }

    public static LoxClass load(JsonObject data, List<LoxClass> enclosed, String pck) {
        String name = GsonHelper.getAsString(data, "name");
        ImmutableMap<String, DataMethodContainer> methods = DataMethodContainer.load(data, name, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = DataMethodContainer.load(data, name, "staticMethods");

        ImmutableMap<String, GeneratedField> staticFields = GeneratedField.loadFieldMap(data, "staticFields");

        LoxClass[] implemented = GsonHelper.getAsJsonArray(data, "implemented").asList().stream().map(JsonElement::getAsString).map(VarTypeManager::getClassForName).toArray(LoxClass[]::new);
        return new GeneratedInterface(
                methods,
                staticMethods,
                staticFields,
                implemented,
                enclosed.stream().collect(Collectors.toMap(LoxClass::name, Function.identity())),
                name,
                pck
        );
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

    @Override
    public LoxClass getStaticFieldType(String name) {
        return allStaticFields.get(name).getType();
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
    public Map<String, ? extends MethodContainer> getDeclaredMethods() {
        return Map.of();
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.has(name);
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return allMethods.get(name).getMethodByOrdinal(ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        return allMethods.get(name).getMethodOrdinal(types);
    }

    @Override
    public LoxClass[] interfaces() {
        return parentInterfaces;
    }

    @Override
    public boolean hasEnclosing(String name) {
        return enclosing.containsKey(name);
    }

    @Override
    public LoxClass getEnclosing(String name) {
        return enclosing.get(name);
    }

    @Override
    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "interface");
        object.addProperty("name", name);
        object.add("methods", DataMethodContainer.saveMethods(allMethods, cacheBuilder));
        object.add("staticMethods", staticMethods.save(cacheBuilder));
        {
            JsonArray parentInterfaces = new JsonArray();
            Arrays.stream(this.parentInterfaces).map(LoxClass::absoluteName).forEach(parentInterfaces::add);
            object.add("implemented", parentInterfaces);
        }
        {
            JsonObject staticFields = new JsonObject();
            allStaticFields.forEach((name, field) -> staticFields.add(name, field.cache(cacheBuilder)));
            object.add("staticFields", staticFields);
        }
        return object;
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @Override
    public CacheableClass[] enclosing() {
        return enclosing.values().toArray(new CacheableClass[0]);
    }
}
