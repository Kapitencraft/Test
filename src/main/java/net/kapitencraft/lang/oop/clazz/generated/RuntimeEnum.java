package net.kapitencraft.lang.oop.clazz.generated;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.EnumClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
import net.kapitencraft.lang.oop.clazz.inst.DynamicClassInstance;
import net.kapitencraft.lang.oop.field.RuntimeEnumConstant;
import net.kapitencraft.lang.oop.field.RuntimeField;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.RuntimeCallable;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.lang.tool.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RuntimeEnum implements EnumClass {
    private Map<String, DynamicClassInstance> constants;
    DynamicClassInstance[] constantData;

    private final GeneratedMethodMap methods;

    private final MethodLookup lookup;

    private final ConstructorContainer constructor;

    private final Map<String, RuntimeField> allFields;
    private final Map<String, RuntimeEnumConstant> enumConstants;
    private final Map<String, RuntimeField> allStaticFields;

    private final Map<String, ClassReference> enclosing;

    private final ClassReference[] implemented;
    private final String name;
    private final String packageRepresentation;

    private final RuntimeAnnotationClassInstance[] annotations;

    public RuntimeEnum(Map<String, DataMethodContainer> methods,
                       List<ScriptedCallable> constructorData,
                       Map<String, RuntimeField> allFields,
                       Function<ScriptedClass, Map<String, RuntimeEnumConstant>> enumConstants,
                       Map<String, RuntimeField> allStaticFields,
                       Map<String, ClassReference> enclosing, ClassReference[] implemented, String name, String packageRepresentation, RuntimeAnnotationClassInstance[] annotations) {
        this.methods = new GeneratedMethodMap(methods);
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

    public static RuntimeEnum load(JsonObject data, List<ClassReference> enclosed, String pck) {
        String name = GsonHelper.getAsString(data, "name");
        ClassReference[] implemented = ClassLoader.loadInterfaces(data);

        ImmutableMap<String, DataMethodContainer> methods = DataMethodContainer.load(data, name, "methods");

        List<ScriptedCallable> constructorData = new ArrayList<>();
        GsonHelper.getAsJsonArray(data, "constructors").asList().stream().map(JsonElement::getAsJsonObject).map(RuntimeCallable::load).forEach(constructorData::add);

        ImmutableMap<String, RuntimeField> fields = RuntimeField.loadFieldMap(data, "fields");
        ImmutableMap<String, RuntimeField> staticFields = RuntimeField.loadFieldMap(data, "staticFields");
        Function<ScriptedClass, Map<String, RuntimeEnumConstant>> enumConstants = RuntimeEnumConstant.loadFieldMap(data, "enumConstants");

        Map<String, ClassReference> enclosedClasses = enclosed.stream().collect(Collectors.toMap(ClassReference::name, Function.identity()));

        RuntimeAnnotationClassInstance[] annotations = CacheLoader.readAnnotations(data);

        return new RuntimeEnum(
                methods, constructorData,
                fields,
                enumConstants,
                staticFields,
                enclosedClasses,
                implemented, name,
                pck, annotations);
    }

    @Override
    public ClassReference[] enclosed() {
        return new ClassReference[0];
    }

    @Override
    public Map<String, ? extends ScriptedField> enumConstants() {
        return enumConstants;
    }

    @Override
    public void setConstantValues(Map<String, DynamicClassInstance> constants) {
        this.constants = constants;
        constantData = this.constants.values().toArray(new DynamicClassInstance[0]);
    }

    @Override
    public DynamicClassInstance[] getConstants() {
        return constantData;
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
        return allStaticFields.get(name).type();
    }

    @Override
    public MethodContainer getConstructor() {
        return constructor;
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
    public RuntimeAnnotationClassInstance[] annotations() {
        return annotations;
    }

    @Override
    public short getModifiers() {
        return Modifiers.ENUM;
    }

    @Override
    public ScriptedCallable getMethod(String signature) {
        return Optional.ofNullable(methods.getMethod(signature)).orElseGet(() -> EnumClass.super.getMethod(signature));
    }
}
