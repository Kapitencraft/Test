package net.kapitencraft.lang.run;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.natives.impl.NativeFieldImpl;
import net.kapitencraft.lang.run.natives.impl.NativeMethodImpl;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.natives.NativeExcluded;
import net.kapitencraft.lang.run.natives.NativeClass;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class NativeClassLoader {
    private static final Map<Class<?>, LoxClass> classLookup = new HashMap<>();

    static {
        classLookup.put(Number.class, VarTypeManager.NUMBER);
        classLookup.put(Integer.class, VarTypeManager.INTEGER);
        classLookup.put(int.class, VarTypeManager.INTEGER);
        classLookup.put(Float.class, VarTypeManager.FLOAT);
        classLookup.put(float.class, VarTypeManager.FLOAT);
        classLookup.put(Double.class, VarTypeManager.DOUBLE);
        classLookup.put(double.class, VarTypeManager.DOUBLE);
        classLookup.put(Boolean.class, VarTypeManager.BOOLEAN);
        classLookup.put(boolean.class, VarTypeManager.BOOLEAN);
        classLookup.put(Character.class, VarTypeManager.CHAR);
        classLookup.put(char.class, VarTypeManager.CHAR);
        classLookup.put(Void.class, VarTypeManager.VOID);
        classLookup.put(void.class, VarTypeManager.VOID);
    }

    public static void load() {
        Reflections reflections = new Reflections("net.kapitencraft.lang.natives");

        Set<Class<?>> nativeClasses = reflections.getTypesAnnotatedWith(NativeClass.class);
        nativeClasses.forEach(NativeClassLoader::createNativeClass);
    }

    public static void createNativeClass(Class<?> clazz) {
        //TODO complete

        if (clazz.isAnnotationPresent(NativeClass.class)) {
            NativeClass nativeClass = clazz.getAnnotation(NativeClass.class);
            String pck = nativeClass.pck();
            String name = nativeClass.name();
            Multimap<String, NativeMethodImpl> map = HashMultimap.create();
            for (Method declaredMethod : clazz.getDeclaredMethods()) {
                if (!declaredMethod.isAnnotationPresent(NativeExcluded.class)) {
                    NativeMethodImpl impl = new NativeMethodImpl(
                            Arrays.stream(declaredMethod.getParameterTypes()).map(NativeClassLoader::lookupClass).toList(),
                            lookupClass(declaredMethod.getReturnType()),
                            Modifier.isFinal(declaredMethod.getModifiers()),
                            Modifier.isAbstract(declaredMethod.getModifiers())
                    ) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            try {
                                declaredMethod.invoke(unwrapClassInstance(environment.getThis()), arguments.stream().map(NativeClassLoader::unwrapClassInstance).toArray(Object[]::new));
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                System.err.println("error executing method: " + e.getMessage());
                            }
                            return null;
                        }
                    };
                }
            }
            Map<String, NativeFieldImpl> fields = new HashMap<>();
            Map<String, NativeFieldImpl> staticFields = new HashMap<>();
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (!declaredField.isAnnotationPresent(NativeExcluded.class)) {
                    NativeFieldImpl impl = new NativeFieldImpl(
                            lookupClass(declaredField.getType()),
                            Modifier.isFinal(declaredField.getModifiers())
                    );
                    if (Modifier.isStatic(declaredField.getModifiers()))
                        staticFields.put(declaredField.getName(), impl);
                    else
                        fields.put(declaredField.getName(), impl);
                }
            }
        } else
            System.err.printf("can not create native class for '%s': class not annotated with 'NativeClass'", clazz.getCanonicalName());
    }

    private static Object unwrapClassInstance(Object in) {
        //if (in instanceof NativeClassInstanceImpl instance) {
        //    return instance.getNativeValue();
        //}
        return in;
    }

    public static ClassReference lookupClass(Class<?> aClass) {
        int arrayCount = 0;
        while (aClass.isArray()) {
            arrayCount++;
            aClass = aClass.getComponentType();
        }
        if (!classLookup.containsKey(aClass)) throw new RuntimeException("class not registered");
        ClassReference c = classLookup.get(aClass).reference();
        while (arrayCount > 0) {
            c = c.array();
            arrayCount--;
        }
        return c;
    }

}
