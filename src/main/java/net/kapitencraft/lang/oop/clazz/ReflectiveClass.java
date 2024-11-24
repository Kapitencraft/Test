package net.kapitencraft.lang.oop.clazz;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import net.kapitencraft.lang.oop.field.LoxField;
import net.kapitencraft.lang.oop.field.ReflectiveField;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.clazz.inst.ReflectiveClassInstance;
import net.kapitencraft.lang.oop.method.MethodMap;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.method.ReflectiveConstructor;
import net.kapitencraft.lang.oop.method.ReflectiveMethod;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.oop.method.builder.MethodContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReflectiveClass<T> implements LoxClass {

    private final Class<T> target;
    private final ReflectiveClass<? super T> superclass;
    private final Map<String, ReflectiveClass<?>> enclosed;
    private final ConstructorContainer constructor;
    private final Map<String, ReflectiveField> fields;
    private final Map<String, ReflectiveField> staticFields;
    private final MethodMap methods;
    private final Map<String, DataMethodContainer> staticMethods;

    public ReflectiveClass(Class<T> target) {
        this.target = target;
        this.enclosed = Arrays.stream(target.getDeclaredClasses()).map(ReflectiveClass::new).collect(Collectors.toMap(LoxClass::name, Function.identity()));

        this.superclass = VarTypeManager.createOrGetLookup(target.getSuperclass());
        List<Method> methods = new ArrayList<>();
        List<Method> staticMethods = new ArrayList<>();
        for (Method method : target.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()))
                staticMethods.add(method);
            else methods.add(method);
        }

        List<Field> fields = new ArrayList<>();
        List<Field> staticFields = new ArrayList<>();
        for (Field field : target.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()))
                staticFields.add(field);
            else fields.add(field);
        }

        this.fields = loadFields(fields);
        this.staticFields = loadFields(staticFields);

        this.methods = new MethodMap(loadMethods(methods));
        this.staticMethods = loadMethods(staticMethods);
        this.constructor = new ConstructorContainer(Arrays.stream(target.getDeclaredConstructors()).map(ReflectiveConstructor::new).toArray(ReflectiveConstructor[]::new));
    }

    private ImmutableMap<String, ReflectiveField> loadFields(List<Field> source) {

        ImmutableMap.Builder<String, ReflectiveField> fieldsBuilder = new ImmutableMap.Builder<>();
        source.forEach(field -> {
            String name = field.getName();
            fieldsBuilder.put(name, new ReflectiveField(field));
        });
        return fieldsBuilder.build();
    }

    private ImmutableMap<String, DataMethodContainer> loadMethods(List<Method> source) {
        Multimap<String, ReflectiveMethod> methodsBuilder = HashMultimap.create();
        source.forEach(method -> {
            String name = method.getName();
            methodsBuilder.put(name, new ReflectiveMethod(method));
        });
        ImmutableMap.Builder<String, DataMethodContainer> builder = new ImmutableMap.Builder<>();
        for (String s : methodsBuilder.keySet()) {
            builder.put(s, new DataMethodContainer(methodsBuilder.get(s).toArray(ReflectiveMethod[]::new)));
        }

        return builder.build();
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
        return target.getSimpleName();
    }

    @Override
    public String packageRepresentation() {
        return "";
    }

    @Override
    public LoxClass superclass() {
        return superclass;
    }

    @Override
    public LoxClass getStaticFieldType(String name) {
        return staticFields.get(name).getType();
    }

    @Override
    public ScriptedCallable getStaticMethodByOrdinal(String name, int ordinal) {
        return staticMethods.get(name).getMethodByOrdinal(ordinal);
    }

    @Override
    public int getStaticMethodOrdinal(String name, List<? extends LoxClass> args) {
        return staticMethods.get(name).getMethodOrdinal(args);
    }

    @Override
    public boolean hasStaticMethod(String name) {
        return staticMethods.containsKey(name) || superclass.hasStaticMethod(name);
    }

    @Override
    public MethodContainer getConstructor() {
        return constructor;
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(target.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(target.getModifiers());
    }

    @Override
    public boolean isInterface() {
        return target.isInterface();
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

    @Override
    public ClassInstance createNativeInst(List<Object> params, int ordinal, Interpreter interpreter) {
        return new ReflectiveClassInstance<>(this, params, ordinal, interpreter);
    }
}
