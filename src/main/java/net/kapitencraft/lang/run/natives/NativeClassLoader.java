package net.kapitencraft.lang.run.natives;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.oop.field.NativeField;
import net.kapitencraft.lang.run.natives.impl.NativeClassImpl;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.natives.impl.NativeConstructor;
import net.kapitencraft.lang.run.natives.impl.NativeFieldImpl;
import net.kapitencraft.lang.run.natives.impl.NativeMethod;
import net.kapitencraft.lang.tool.Util;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.*;

public class NativeClassLoader {
    @ApiStatus.Internal
    private static final Map<Class<?>, ClassReference> classLookup = new HashMap<>();

    static {
        registerNative(VarTypeManager.NUMBER, Number.class);
        registerNative(VarTypeManager.INTEGER, Integer.class, int.class);
        registerNative(VarTypeManager.FLOAT, Float.class, float.class);
        registerNative(VarTypeManager.DOUBLE, Double.class, double.class);
        registerNative(VarTypeManager.BOOLEAN, Boolean.class, boolean.class);
        registerNative(VarTypeManager.CHAR, Character.class, char.class);
        registerNative(VarTypeManager.VOID, Void.class, void.class);
    }

    private static boolean hadError = false;

    private static void registerNative(PrimitiveClass target, Class<?>... types) {
        for (Class<?> type : types) {
            classLookup.put(type, target.reference());
        }
    }

    @ApiStatus.Internal
    public static void load() {
        System.out.println("loading natives...");
        Reflections reflections = new Reflections("net.kapitencraft.lang.run.natives.scripted");

        List<ClassObj> nativeClasses = new ArrayList<>();

        Collection<Class<?>> plugins = reflections.getTypesAnnotatedWith(ScriptedPlugin.class);
        System.out.println("Found " + plugins.size() + " plugin(s)");
        ClassRegistration registration = new ClassRegistration(nativeClasses);
        plugins.forEach(c -> handlePlugin(c, registration));
        
        registerAnnotated(nativeClasses, reflections.getTypesAnnotatedWith(NativeClass.class));

        nativeClasses.forEach(ClassObj::registerReference);
        nativeClasses.forEach(ClassObj::loadClass);

        if (hadError) {
            System.out.println("NativeLoader detected broken load state. shutting down!");
            System.exit(65);
        }
        System.out.println("Total registered classes: " + nativeClasses.size());
    }

    private static void registerAnnotated(List<ClassObj> nativeClasses, Set<Class<?>> annotated) {
        annotated.stream().map(AnnotatedClassObj::new).forEach(nativeClasses::add);
    }

    private static void handlePlugin(Class<?> c, ClassRegistration registration) {
        try {
            Method method = c.getMethod("registerClasses", ClassRegistration.class);
            method.invoke(null, registration);
        } catch (NoSuchMethodException ignored) {} catch (InvocationTargetException | IllegalAccessException e) {
            System.out.println("unable to execute plugin method 'registerClasses': " + e.getMessage());
        }

    }

    private static void setupLookup(Class<?> clazz, String name, String pck) {
        ClassReference reference = VarTypeManager.getOrCreateClass(name, pck);
        classLookup.put(clazz, reference);
    }

    /**
     * creates and registers a native class for the given java class
     * <br>Developers should use {@link ScriptedPlugin} or {@link NativeClass}
     * @param clazz the target to create & register
     */
    private static void createNativeClass(Class<?> clazz, String className, String pck, @Nullable String[] capturedMethods, @Nullable String[] capturedFields) {
        try {
            Multimap<String, NativeMethod> methods = HashMultimap.create();
            Multimap<String, NativeMethod> staticMethods = HashMultimap.create();
            for (Method declaredMethod : clazz.getDeclaredMethods()) {
                if (Modifier.isPublic(declaredMethod.getModifiers()) && !declaredMethod.isAnnotationPresent(Excluded.class) && (capturedMethods == null || Util.arrayContains(capturedMethods, declaredMethod.getName()))) {
                    try {
                        boolean isStatic = Modifier.isStatic(declaredMethod.getModifiers());
                        String methodName = declaredMethod.isAnnotationPresent(Rename.class) ? declaredMethod.getAnnotation(Rename.class).value() : declaredMethod.getName();
                        NativeMethod method = new NativeMethod(
                                getClassOrThrow(declaredMethod.getReturnType()),
                                Arrays.stream(declaredMethod.getParameterTypes()).map(NativeClassLoader::getClassOrThrow).toArray(ClassReference[]::new),
                                declaredMethod,
                                !isStatic,
                                Modifiers.fromJavaMods(declaredMethod.getModifiers())
                        );
                        if (isStatic)
                            staticMethods.put(methodName, method);
                        else
                            methods.put(methodName, method);
                    } catch (RuntimeException ignored) {
                    }
                }
            }


            Map<String, NativeField> fields = new HashMap<>();
            Map<String, NativeField> staticFields = new HashMap<>();
            for (Field declaredField : clazz.getDeclaredFields()) {
                try {
                    String fieldName = declaredField.isAnnotationPresent(Rename.class) ? declaredField.getAnnotation(Rename.class).value() : declaredField.getName();
                    if (Modifier.isPublic(declaredField.getModifiers()) && !declaredField.isAnnotationPresent(Excluded.class) && (capturedFields == null || Util.arrayContains(capturedFields, fieldName))) {
                        NativeField impl = new NativeField(
                                getClassOrThrow(declaredField.getType()),
                                Modifier.isFinal(declaredField.getModifiers()),
                                declaredField
                        );
                        if (Modifier.isStatic(declaredField.getModifiers()))
                            staticFields.put(fieldName, impl);
                        else
                            fields.put(fieldName, impl);
                    }
                } catch (RuntimeException ignored) {
                }
            }
            ClassReference type = getClassOrThrow(clazz);
            List<NativeConstructor> constructors = new ArrayList<>();
            for (Constructor<?> constructor : clazz.getConstructors()) {
                try {
                    if (!constructor.isAnnotationPresent(Excluded.class)) {
                        NativeConstructor method = new NativeConstructor(
                                type,
                                Arrays.stream(constructor.getParameterTypes()).map(NativeClassLoader::getClassOrThrow).toArray(ClassReference[]::new),
                                constructor
                        );
                        constructors.add(method);
                    }
                } catch (RuntimeException ignored) {
                }
            }

            type.setTarget(new NativeClassImpl(className, pck,
                    bakeMethods(staticMethods), staticFields,
                    bakeMethods(methods), fields,
                    new DataMethodContainer(constructors.toArray(new NativeConstructor[0])),
                    getClassOrThrow(clazz.getSuperclass()),
                    extractInterfaces(clazz.getInterfaces()),
                    Modifiers.fromJavaMods(clazz.getModifiers())
            ));
        } catch (Exception e) {
            System.err.println("Failed to load class '" + clazz.getName() + "': " + e.getMessage());
            hadError = true;
        }
    }

    private static ClassReference[] extractInterfaces(Class<?>[] interfaces) {
        List<ClassReference> extensions = new ArrayList<>();
        for (Class<?> c : interfaces) {
            getClass(c).ifPresent(extensions::add);
        }
        return extensions.toArray(ClassReference[]::new);
    }

    private static ClassReference getClassOrThrow(Class<?> aClass) {
        if (aClass == null) return null;
        int arrayCount = 0;
        while (aClass.isArray()) {
            arrayCount++;
            aClass = aClass.getComponentType();
        }
        if (!classLookup.containsKey(aClass))
            throw new RuntimeException("class '" + aClass.getName() + "' not registered");
        ClassReference c = classLookup.get(aClass);
        while (arrayCount > 0) {
            c = c.array();
            arrayCount--;
        }
        return c;
    }

    /**
     * queries the given class for lookup
     * @param aClass the class to query
     * @return an optional of the gotten reference
     */
    public static Optional<ClassReference> getClass(Class<?> aClass) {
        if (aClass == null) return Optional.empty();
        int arrayCount = 0;
        while (aClass.isArray()) {
            arrayCount++;
            aClass = aClass.getComponentType();
        }

        ClassReference c = classLookup.get(aClass);
        if (c == null) return Optional.empty();
        while (arrayCount > 0) {
            c = c.array();
            arrayCount--;
        }
        return Optional.of(c);
    }

    /**
     * extracts a native value out of a {@link NativeClassInstance} wrapper, or throws if it can't
     */
    public static Object extractNative(Object reference) {
        if (reference instanceof Number ||
                reference instanceof String ||
                reference == null ||
                reference instanceof Boolean ||
                reference instanceof Character
        ) return reference;
        if (reference instanceof NativeClassInstance NCI) {
            return NCI.getObject();
        }
        throw new IllegalArgumentException("cannot extract native from '" + reference + "'");
    }

    private static Map<String, DataMethodContainer> bakeMethods(Multimap<String, NativeMethod> methods) {
        ImmutableMap.Builder<String, DataMethodContainer> builder = new ImmutableMap.Builder<>();
        for (String s : methods.keySet()) {
            Collection<NativeMethod> nativeMethods = methods.get(s);
            DataMethodContainer container = new DataMethodContainer(nativeMethods.toArray(new NativeMethod[0]));
            builder.put(s, container);
        }
        return builder.build();
    }

    public static Object[] extractNatives(List<Object> in) {
        return in.stream().map(NativeClassLoader::extractNative).toArray();
    }

    interface ClassObj {
        void loadClass();

        void registerReference();
    }

    static class AnnotatedClassObj implements ClassObj {
        private final Class<?> target;

        AnnotatedClassObj(Class<?> target) {
            this.target = target;
        }

        @Override
        public void loadClass() {
            if (target.isAnnotationPresent(NativeClass.class)) {
                NativeClass nativeClass = target.getAnnotation(NativeClass.class);
                String pck = nativeClass.pck();
                String className = nativeClass.name();
                if (className.isEmpty()) className = target.getSimpleName();
                NativeClassLoader.createNativeClass(target, className, pck, null, null);
            } else {
                System.err.printf("can not create native class for '%s': missing NativeClass annotation", target.getCanonicalName());
                System.out.println();
            }
        }

        @Override
        public void registerReference() {
            if (target.isAnnotationPresent(NativeClass.class)) {
                NativeClass nativeClass = target.getAnnotation(NativeClass.class);
                String pck = nativeClass.pck();
                String className = nativeClass.name();
                if (className.isEmpty()) className = target.getSimpleName();
                NativeClassLoader.setupLookup(target, className, pck);
            }
        }
    }

    static class RegisteredClassObj implements ClassObj {
        private final Class<?> target;
        private final String[] capturedMethods, capturedFields;
        private final String name, pck;

        RegisteredClassObj(Class<?> target, String[] capturedMethods, String[] capturedFields, String name, String pck) {
            this.target = target;
            this.capturedMethods = capturedMethods;
            this.capturedFields = capturedFields;
            this.name = name;
            this.pck = pck;
        }

        @Override
        public void loadClass() {
            try {
                NativeClassLoader.createNativeClass(target, name, pck, capturedMethods, capturedFields);
            } catch (Exception e) {
                System.err.println("Failed to load class '" + target.getName() + "': " + e.getMessage());
            }
        }

        @Override
        public void registerReference() {
            NativeClassLoader.setupLookup(target, name, pck);
        }
    }
}
