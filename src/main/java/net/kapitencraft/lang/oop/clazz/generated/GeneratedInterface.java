package net.kapitencraft.lang.oop.clazz.generated;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.LoxInterface;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.GsonHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GeneratedInterface implements LoxInterface, CacheableClass {
    private final MethodMap methods;
    private final MethodLookup lookup;
    private final Map<String, DataMethodContainer> allMethods;
    private final MethodMap staticMethods;
    private final Map<String, GeneratedField> allStaticFields;

    private final LoxClass[] parentInterfaces;

    private final Map<String, LoxClass> enclosing;

    private final String name;
    private final String packageRepresentation;

    public GeneratedInterface(Map<String, DataMethodContainer> allMethods, Map<String, DataMethodContainer> allStaticMethods, Map<String, GeneratedField> allStaticFields, LoxClass[] parentInterfaces, Map<String, LoxClass> enclosing, String name, String packageRepresentation) {
        this.methods = new MethodMap(allMethods);
        this.allMethods = allMethods;
        this.staticMethods = new MethodMap(allStaticMethods);
        this.allStaticFields = allStaticFields;
        this.parentInterfaces = parentInterfaces;
        this.enclosing = enclosing;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public static LoxClass load(JsonObject data, List<LoxClass> enclosed, String pck) {
        String name = GsonHelper.getAsString(data, "name");
        ImmutableMap<String, DataMethodContainer> methods = DataMethodContainer.load(data, name, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = DataMethodContainer.load(data, name, "staticMethods");

        ImmutableMap<String, GeneratedField> staticFields = GeneratedField.loadFieldMap(data, "staticFields");

        LoxClass[] implemented = GsonHelper.getAsJsonArray(data, "interfaces").asList().stream().map(JsonElement::getAsString).map(VarTypeManager::getClassForName).toArray(LoxClass[]::new);
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
        LoxInterface.super.clInit();
        this.enclosing.values().forEach(LoxClass::clInit);
        Interpreter.INSTANCE.popCall();
    }

    @Override
    public Map<String, ? extends LoxField> staticFields() {
        return allStaticFields;
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
        checkInit();
        return staticMethods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        checkInit();
        return staticMethods.getMethodOrdinal(name, args);
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.has(name);
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        checkInit();
        return lookup.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, List<LoxClass> types) {
        checkInit();
        return lookup.getMethodOrdinal(name, types);
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
    public MethodMap getMethods() {
        return methods;
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
            object.add("interfaces", parentInterfaces);
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

    @Override
    public MethodLookup methods() {
        return lookup;
    }
}
