package net.kapitencraft.lang.oop.clazz;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.field.GeneratedEnumConstant;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GeneratedEnum implements CacheableClass, EnumClass {
    private Map<String, ClassInstance> constants;
    ClassInstance[] constantData;

    private final MethodMap methods;
    private final MethodMap staticMethods;

    private final MethodLookup lookup;

    private final ConstructorContainer constructor;

    private final Map<String, GeneratedField> allFields;
    private final Map<String, GeneratedEnumConstant> enumConstants;
    private final Map<String, GeneratedField> allStaticFields;

    private final Map<String, LoxClass> enclosing;

    private final LoxClass[] implemented;
    private final String name;
    private final String packageRepresentation;

    public GeneratedEnum(Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, ConstructorContainer.Builder constructor, Map<String, GeneratedField> allFields, Map<String, GeneratedEnumConstant> enumConstants, Map<String, GeneratedField> allStaticFields, Map<String, LoxClass> enclosing, LoxClass[] implemented, String name, String packageRepresentation) {
        this.methods = new MethodMap(methods);
        this.staticMethods = new MethodMap(staticMethods);
        this.constructor = constructor.build(this);
        this.allFields = allFields;
        this.enumConstants = enumConstants;
        this.allStaticFields = allStaticFields;
        this.enclosing = enclosing;
        this.implemented = implemented;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public GeneratedEnum(Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods,
                         List<ScriptedCallable> constructorData,
                         Map<String, GeneratedField> allFields,
                         Function<LoxClass, Map<String, GeneratedEnumConstant>> enumConstants,
                         Map<String, GeneratedField> allStaticFields,
                         Map<String, LoxClass> enclosing, LoxClass[] implemented, String name, String packageRepresentation) {
        this.methods = new MethodMap(methods);
        this.staticMethods = new MethodMap(staticMethods);
        this.constructor = ConstructorContainer.fromCache(constructorData, this);
        this.allFields = allFields;
        this.enumConstants = enumConstants.apply(this);
        this.allStaticFields = allStaticFields;
        this.enclosing = enclosing;
        this.implemented = implemented;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public static GeneratedEnum load(JsonObject data, List<LoxClass> enclosed, String pck) {
        String name = GsonHelper.getAsString(data, "name");
        LoxClass[] implemented = GsonHelper.getAsJsonArray(data, "interfaces").asList().stream().map(JsonElement::getAsString).map(VarTypeManager::getClassForName).toArray(LoxClass[]::new);

        ImmutableMap<String, DataMethodContainer> methods = DataMethodContainer.load(data, name, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = DataMethodContainer.load(data, name, "staticMethods");

        List<ScriptedCallable> constructorData = new ArrayList<>();
        GsonHelper.getAsJsonArray(data, "constructors").asList().stream().map(JsonElement::getAsJsonObject).map(GeneratedCallable::load).forEach(constructorData::add);

        ImmutableMap<String, GeneratedField> fields = GeneratedField.loadFieldMap(data, "fields");
        ImmutableMap<String, GeneratedField> staticFields = GeneratedField.loadFieldMap(data, "staticFields");
        Function<LoxClass, Map<String, GeneratedEnumConstant>> enumConstants = GeneratedEnumConstant.loadFieldMap(data, "enumConstants");

        Map<String, LoxClass> enclosedClasses = enclosed.stream().collect(Collectors.toMap(LoxClass::name, Function.identity()));

        return new GeneratedEnum(
                methods, staticMethods, constructorData,
                fields,
                enumConstants,
                staticFields,
                enclosedClasses,
                implemented, name,
                pck);
    }


    @Override
    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "enum");
        object.addProperty("name", name);
        {
            JsonArray parentInterfaces = new JsonArray();
            Arrays.stream(this.implemented).map(LoxClass::absoluteName).forEach(parentInterfaces::add);
            object.add("interfaces", parentInterfaces);
        }
        object.add("methods", methods.save(cacheBuilder));
        object.add("staticMethods", staticMethods.save(cacheBuilder));
        object.add("constructors", constructor.cache(cacheBuilder));
        {
            JsonObject constants = new JsonObject();
            enumConstants.forEach((name, field) -> constants.add(name, field.cache(cacheBuilder)));
            object.add("enumConstants", constants);
        }
        {
            JsonObject fields = new JsonObject();
            allFields.forEach((name, field) -> fields.add(name, field.cache(cacheBuilder)));
            object.add("fields", fields);
        }
        {
            JsonObject staticFields = new JsonObject();
            allStaticFields.forEach((name, field) -> staticFields.add(name, field.cache(cacheBuilder)));
            object.add("staticFields", staticFields);
        }

        return object;
    }

    @Override
    public CacheableClass[] enclosing() {
        return new CacheableClass[0];
    }

    @Override
    public MethodLookup methods() {
        return lookup;
    }

    @Override
    public Map<String, ? extends LoxField> enumConstants() {
        return enumConstants;
    }

    @Override
    public void setConstantValues(Map<String, ClassInstance> constants) {
        this.constants = constants;
        constantData = this.constants.values().toArray(new ClassInstance[0]);
    }

    @Override
    public Map<String, ClassInstance> getConstantValues() {
        return constants;
    }

    @Override
    public ClassInstance[] getConstants() {
        return constantData;
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
    public Map<String, ? extends LoxField> staticFields() {
        return Util.mergeMaps(allStaticFields, enumConstants);
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
        return VarTypeManager.ENUM.get();
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return allStaticFields.get(name).getType();
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        checkInit();
        return staticMethods.has(name) ? Optional.ofNullable(staticMethods.getMethodByOrdinal(name, ordinal)).orElseGet(() -> EnumClass.super.getStaticMethodByOrdinal(name, ordinal)) : EnumClass.super.getStaticMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        checkInit();
        return staticMethods.has(name) ? staticMethods.getMethodOrdinal(name, args) : EnumClass.super.getStaticMethodOrdinal(name, args);
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.has(name);
    }

    @Override
    public MethodContainer getConstructor() {
        return constructor;
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
        return false;
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
    public void clInit() {
        if (Interpreter.suppressClassLoad) return;
        Interpreter.INSTANCE.pushCallIndex(-1);
        Interpreter.INSTANCE.pushCall(this.absoluteName(), "<clinit>", this.name());
        EnumClass.super.clInit();
        this.enclosing.values().forEach(LoxClass::clInit);
        Interpreter.INSTANCE.popCall();
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
}
