package net.kapitencraft.lang.oop.clazz.generated;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.EnumClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.field.GeneratedEnumConstant;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.GeneratedCallable;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Util;
import org.checkerframework.checker.signature.qual.CanonicalNameOrEmpty;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GeneratedEnum implements CacheableClass, EnumClass {
    private Map<String, ClassInstance> constants;
    ClassInstance[] constantData;

    private final GeneratedMethodMap methods;
    private final GeneratedMethodMap staticMethods;

    private final MethodLookup lookup;

    private final ConstructorContainer constructor;

    private final Map<String, GeneratedField> allFields;
    private final Map<String, GeneratedEnumConstant> enumConstants;
    private final Map<String, GeneratedField> allStaticFields;

    private final Map<String, ClassReference> enclosing;

    private final ClassReference[] implemented;
    private final String name;
    private final String packageRepresentation;

    private final AnnotationClassInstance[] annotations;

    public GeneratedEnum(Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, ConstructorContainer.Builder constructor, Map<String, GeneratedField> allFields, Map<String, GeneratedEnumConstant> enumConstants, Map<String, GeneratedField> allStaticFields, Map<String, ClassReference> enclosing, ClassReference[] implemented, String name, String packageRepresentation, AnnotationClassInstance[] annotations) {
        this.methods = new GeneratedMethodMap(methods);
        this.staticMethods = new GeneratedMethodMap(staticMethods);
        this.constructor = constructor.build(this);
        this.allFields = allFields;
        this.enumConstants = enumConstants;
        this.allStaticFields = allStaticFields;
        this.enclosing = enclosing;
        this.implemented = implemented;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.annotations = annotations;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public GeneratedEnum(Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods,
                         List<ScriptedCallable> constructorData,
                         Map<String, GeneratedField> allFields,
                         Function<ScriptedClass, Map<String, GeneratedEnumConstant>> enumConstants,
                         Map<String, GeneratedField> allStaticFields,
                         Map<String, ClassReference> enclosing, ClassReference[] implemented, String name, String packageRepresentation, AnnotationClassInstance[] annotations) {
        this.methods = new GeneratedMethodMap(methods);
        this.staticMethods = new GeneratedMethodMap(staticMethods);
        this.constructor = ConstructorContainer.fromCache(constructorData, this);
        this.allFields = allFields;
        this.enumConstants = enumConstants.apply(this);
        this.allStaticFields = allStaticFields;
        this.enclosing = enclosing;
        this.implemented = implemented;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.annotations = annotations;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public static GeneratedEnum load(JsonObject data, List<ClassReference> enclosed, String pck) {
        String name = GsonHelper.getAsString(data, "name");
        ClassReference[] implemented = ClassLoader.loadInterfaces(data);

        ImmutableMap<String, DataMethodContainer> methods = DataMethodContainer.load(data, name, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = DataMethodContainer.load(data, name, "staticMethods");

        List<ScriptedCallable> constructorData = new ArrayList<>();
        GsonHelper.getAsJsonArray(data, "constructors").asList().stream().map(JsonElement::getAsJsonObject).map(GeneratedCallable::load).forEach(constructorData::add);

        ImmutableMap<String, GeneratedField> fields = GeneratedField.loadFieldMap(data, "fields");
        ImmutableMap<String, GeneratedField> staticFields = GeneratedField.loadFieldMap(data, "staticFields");
        Function<ScriptedClass, Map<String, GeneratedEnumConstant>> enumConstants = GeneratedEnumConstant.loadFieldMap(data, "enumConstants");

        Map<String, ClassReference> enclosedClasses = enclosed.stream().collect(Collectors.toMap(ClassReference::name, Function.identity()));

        AnnotationClassInstance[] annotations = CacheLoader.readAnnotations(data);

        return new GeneratedEnum(
                methods, staticMethods, constructorData,
                fields,
                enumConstants,
                staticFields,
                enclosedClasses,
                implemented, name,
                pck, annotations);
    }


    @Override
    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "enum");
        object.addProperty("name", name);
        {
            JsonArray parentInterfaces = new JsonArray();
            Arrays.stream(this.implemented).map(ClassReference::absoluteName).forEach(parentInterfaces::add);
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

        object.add("annotations", cacheBuilder.cacheAnnotations(this.annotations));

        return object;
    }

    @Override
    public ClassReference[] enclosed() {
        return new ClassReference[0];
    }

    @Override
    public MethodLookup methods() {
        return lookup;
    }

    @Override
    public Map<String, ? extends ScriptedField> enumConstants() {
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
    public Map<String, ? extends ScriptedField> staticFields() {
        return Util.mergeMaps(allStaticFields, enumConstants);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String pck() {
        return packageRepresentation;
    }

    @Override
    public @Nullable ClassReference superclass() {
        return VarTypeManager.ENUM;
    }

    @Override
    public ClassReference getStaticFieldType(String name) {
        return allStaticFields.get(name).getType();
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        checkInit();
        return staticMethods.has(name) ? Optional.ofNullable(staticMethods.getMethodByOrdinal(name, ordinal)).orElseGet(() -> EnumClass.super.getStaticMethodByOrdinal(name, ordinal)) : EnumClass.super.getStaticMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<ClassReference> args) {
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
    public int getMethodOrdinal(String name, List<ClassReference> types) {
        checkInit();
        return lookup.getMethodOrdinal(name, types);
    }

    @Override
    public boolean hasEnclosing(String name) {
        return enclosing.containsKey(name);
    }

    @Override
    public ClassReference getEnclosing(String name) {
        return enclosing.get(name);
    }

    @Override
    public GeneratedMethodMap getMethods() {
        return methods;
    }

    @Override
    public AnnotationClassInstance[] annotations() {
        return annotations;
    }
}
