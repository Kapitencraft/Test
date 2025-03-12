package net.kapitencraft.lang.oop.clazz.generated;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.compiler.MethodLookup;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.CacheableClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.ScriptedInterface;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.oop.field.GeneratedField;
import net.kapitencraft.lang.oop.field.ScriptedField;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GeneratedInterface implements ScriptedInterface, CacheableClass {
    private final GeneratedMethodMap methods;
    private final MethodLookup lookup;
    private final Map<String, DataMethodContainer> allMethods;
    private final GeneratedMethodMap staticMethods;
    private final Map<String, GeneratedField> allStaticFields;

    private final ClassReference[] parentInterfaces;

    private final Map<String, ClassReference> enclosing;

    private final String name;
    private final String packageRepresentation;

    private final AnnotationClassInstance[] annotations;

    public GeneratedInterface(Map<String, DataMethodContainer> allMethods, Map<String, DataMethodContainer> allStaticMethods, Map<String, GeneratedField> allStaticFields, ClassReference[] parentInterfaces, Map<String, ClassReference> enclosing, String name, String packageRepresentation, AnnotationClassInstance[] annotations) {
        this.methods = new GeneratedMethodMap(allMethods);
        this.allMethods = allMethods;
        this.staticMethods = new GeneratedMethodMap(allStaticMethods);
        this.allStaticFields = allStaticFields;
        this.parentInterfaces = parentInterfaces;
        this.enclosing = enclosing;
        this.name = name;
        this.packageRepresentation = packageRepresentation;
        this.annotations = annotations;
        this.lookup = MethodLookup.createFromClass(this);
    }

    public static ScriptedClass load(JsonObject data, List<ClassReference> enclosed, String pck) {
        String name = GsonHelper.getAsString(data, "name");
        ImmutableMap<String, DataMethodContainer> methods = DataMethodContainer.load(data, name, "methods");
        ImmutableMap<String, DataMethodContainer> staticMethods = DataMethodContainer.load(data, name, "staticMethods");

        ImmutableMap<String, GeneratedField> staticFields = GeneratedField.loadFieldMap(data, "staticFields");

        ClassReference[] implemented = ClassLoader.loadInterfaces(data);

        AnnotationClassInstance[] annotations = CacheLoader.readAnnotations(data);

        return new GeneratedInterface(
                methods,
                staticMethods,
                staticFields,
                implemented,
                enclosed.stream().collect(Collectors.toMap(ClassReference::name, Function.identity())),
                name,
                pck,
                annotations
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
    public Map<String, ? extends ScriptedField> staticFields() {
        return allStaticFields;
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
        return null;
    }

    @Override
    public ClassReference getStaticFieldType(String name) {
        return allStaticFields.get(name).getType();
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        checkInit();
        return staticMethods.getMethodByOrdinal(name, ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, ClassReference[] args) {
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
    public int getMethodOrdinal(String name, ClassReference[] types) {
        checkInit();
        return lookup.getMethodOrdinal(name, types);
    }

    @Override
    public ClassReference[] interfaces() {
        return parentInterfaces;
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

    @Override
    public JsonObject save(CacheBuilder cacheBuilder) {
        JsonObject object = new JsonObject();
        object.addProperty("TYPE", "interface");
        object.addProperty("name", name);
        object.add("methods", DataMethodContainer.saveMethods(allMethods, cacheBuilder));
        object.add("staticMethods", staticMethods.save(cacheBuilder));
        {
            JsonArray parentInterfaces = new JsonArray();
            Arrays.stream(this.parentInterfaces).map(ClassReference::absoluteName).forEach(parentInterfaces::add);
            object.add("interfaces", parentInterfaces);
        }
        {
            JsonObject staticFields = new JsonObject();
            allStaticFields.forEach((name, field) -> staticFields.add(name, field.cache(cacheBuilder)));
            object.add("staticFields", staticFields);
        }
        object.add("annotations", cacheBuilder.cacheAnnotations(annotations));

        return object;
    }

    @Override
    public ClassReference[] enclosed() {
        return enclosing.values().toArray(new ClassReference[0]);
    }

    @Override
    public MethodLookup methods() {
        return lookup;
    }
}
