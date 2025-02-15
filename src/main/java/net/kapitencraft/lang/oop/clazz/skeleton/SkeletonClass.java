package net.kapitencraft.lang.oop.clazz.skeleton;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.field.SkeletonField;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.SkeletonMethod;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SkeletonClass implements ScriptedClass {
    private final String name;
    private final String pck;

    private final ClassReference superclass;
    private final Map<String, SkeletonField> fields;
    private final Map<String, SkeletonField> staticFields;

    private final Map<String, ClassReference> enclosed;

    private final GeneratedMethodMap methods;
    private final Map<String, DataMethodContainer> staticMethods;
    private final ConstructorContainer constructor;

    private final short modifiers;

    public SkeletonClass(String name, String pck, ClassReference superclass,
                         Map<String, SkeletonField> staticFields, Map<String, SkeletonField> fields,
                         Map<String, ClassReference> enclosed,
                         Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, ConstructorContainer.Builder constructor,
                         short modifiers) {
        this.name = name;
        this.pck = pck;
        this.superclass = superclass;
        this.staticFields = staticFields;
        this.fields = fields;
        this.enclosed = enclosed;
        this.methods = new GeneratedMethodMap(methods);
        this.staticMethods = staticMethods;
        this.constructor = constructor.build(this);
        this.modifiers = modifiers;
    }

    public SkeletonClass(String name, String pck, ClassReference superclass,
                         Map<String, SkeletonField> staticFields, Map<String, SkeletonField> fields,
                         Map<String, ClassReference> enclosed,
                         Map<String, DataMethodContainer> methods, Map<String, DataMethodContainer> staticMethods, ConstructorContainer constructor,
                         short modifiers) {
        this.name = name;
        this.pck = pck;
        this.superclass = superclass;
        this.staticFields = staticFields;
        this.fields = fields;
        this.enclosed = enclosed;
        this.methods = new GeneratedMethodMap(methods);
        this.staticMethods = staticMethods;
        this.constructor = constructor;
        this.modifiers = modifiers;
    }

    public static SkeletonClass fromCache(JsonObject data, String pck, ClassReference[] enclosed) {
        String name = GsonHelper.getAsString(data, "name");
        ClassReference superclass = ClassLoader.loadClassReference(data, "superclass");

        ImmutableMap<String, DataMethodContainer> methods = SkeletonMethod.readFromCache(data, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = SkeletonMethod.readFromCache(data, "staticMethods");

        ConstructorContainer constructorContainer = new ConstructorContainer(
                GsonHelper.getAsJsonArray(data, "constructors").asList()
                        .stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(SkeletonMethod::fromJson)
                        .toArray(SkeletonMethod[]::new)
        );
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

        short modifiers = data.has("modifiers") ? GsonHelper.getAsShort(data, "modifiers") : 0;

        return new SkeletonClass(name, pck, superclass,
                staticFields.build(), fields.build(),
                Arrays.stream(enclosed).collect(Collectors.toMap(ClassReference::name, Function.identity())),
                methods, staticMethods,
                constructorContainer,
                modifiers
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
        return pck + "." + name;
    }

    @Override
    public boolean hasField(String name) {
        return this.fields.containsKey(name) || ScriptedClass.super.hasField(name);
    }

    @Override
    public ClassReference getFieldType(String name) {
        return Optional.ofNullable(this.fields.get(name)).map(SkeletonField::getType).orElseGet(() -> superclass.get().getFieldType(name));
    }

    @Override
    public @Nullable ClassReference superclass() {
        return superclass;
    }

    @Override
    public ClassReference getStaticFieldType(String name) {
        return staticFields.get(name).getType();
    }

    @Override
    public ScriptedCallable getStaticMethod(String name, List<ClassReference> args) {
        return staticMethods.get(name).getMethod(args);
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return staticMethods.get(name).getMethodByOrdinal(ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<ClassReference> args) {
        return staticMethods.get(name).getMethodOrdinal(args);
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.containsKey(name);
    }

    @Override
    public DataMethodContainer getConstructor() {
        return constructor;
    }

    @Override
    public boolean isAbstract() {
        return Modifiers.isAbstract(modifiers);
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(modifiers);
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public ScriptedCallable getMethodByOrdinal(String name, int ordinal) {
        return methods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getMethodOrdinal(String name, List<ClassReference> types) {
        return methods.getMethodOrdinal(name, types);
    }

    @Override
    public boolean hasMethod(String name) {
        return methods.has(name) || ScriptedClass.super.hasMethod(name);
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
    public ClassType getClassType() {
        return ClassType.CLASS;
    }

    @Override
    public AnnotationClassInstance[] annotations() {
        return new AnnotationClassInstance[0];
    }
}
