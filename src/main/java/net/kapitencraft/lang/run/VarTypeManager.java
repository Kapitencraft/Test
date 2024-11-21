package net.kapitencraft.lang.run;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.NativeMethodImpl;
import net.kapitencraft.lang.natives.scripted.lang.*;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.natives.scripted.lang.IndexOutOfBoundsException;
import net.kapitencraft.lang.natives.scripted.lang.SystemClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.oop.clazz.PreviewClass;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.oop.clazz.ReflectiveClass;
import net.kapitencraft.lang.run.natives.NativeClass;
import net.kapitencraft.lang.run.natives.NativeMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class VarTypeManager {
    private static final List<ClassData<?>> data = new ArrayList<>();
    private static final Package root = new Package("");
    private static final Package langRoot = getOrCreatePackage("scripted.lang");
    private static final Map<Class<?>, LoxClass> classLookup = new HashMap<>();
    public static final ReflectiveLoader reflectiveLoader = new ReflectiveLoader();

    public static final LoxClass NUMBER = new PrimitiveClass("num" , null, Number.class);
    public static final LoxClass INTEGER = new PrimitiveClass(NUMBER, "int", 0, Integer.TYPE);
    public static final LoxClass FLOAT = new PrimitiveClass(NUMBER, "float", 0f, Float.TYPE);
    public static final LoxClass DOUBLE = new PrimitiveClass(NUMBER, "double", 0d, Double.TYPE);
    public static final LoxClass BOOLEAN = new PrimitiveClass("bool", false, Boolean.TYPE);
    public static final LoxClass CHAR = new PrimitiveClass("char", ' ', Character.TYPE);
    public static final LoxClass VOID = new PrimitiveClass("void", null, Void.TYPE);

    public static final Supplier<LoxClass> OBJECT = registerMain(ObjectClass::new, "Object");

    public static final Supplier<LoxClass> ENUM = registerMain(EnumClass::new, "Enum");

    public static final Supplier<LoxClass> STRING = registerMain(StringClass::new, "String", String.class);



    public static final Supplier<LoxClass> THROWABLE = registerMain(() -> new ThrowableClass("Throwable", "scripted.lang", null), "Throwable");
    public static final Supplier<LoxClass> STACK_OVERFLOW_EXCEPTION = registerMain(StackOverflowExceptionClass::new, "StackOverflowException");
    public static final Supplier<LoxClass> MISSING_VAR_EXCEPTION = registerMain(MissingVarExceptionClass::new, "MissingVarException");
    public static final Supplier<LoxClass> ARITHMETIC_EXCEPTION = registerMain(ArithmeticExceptionClass::new, "ArithmeticException");
    public static final Supplier<LoxClass> FUNCTION_CALL_ERROR = registerMain(FunctionCallErrorClass::new, "FunctionCallError");
    public static final Supplier<LoxClass> INDEX_OUT_OF_BOUNDS_EXCEPTION = registerMain(IndexOutOfBoundsException::new, "IndexOutOfBoundsException");

    public static final Supplier<LoxClass> SYSTEM = registerMain(SystemClass::new, "System");
    public static final Supplier<LoxClass> MATH = registerMain(MathClass::new, "Math");

    static {
        loadClasses();
    }

    private static void loadClasses() {
        data.forEach(ClassData::create);
    }

    private static final class ClassData<T extends LoxClass> implements Supplier<LoxClass> {
        private final Package pck;
        private final Supplier<T> sup;
        private final Class<?> target;
        private final PreviewClass preview;
        private LoxClass value;

        private ClassData(Package pck, Supplier<T> sup, Class<?> target, PreviewClass preview) {
            this.pck = pck;
            this.sup = sup;
            this.target = target;
            this.preview = preview;
            this.value = preview;
        }

            public void create() {
                LoxClass val = sup.get(); //bruh
                this.value = val;
                preview.apply(val);
                pck.addClass(val.name(), val);
            }

            @Override
            public LoxClass get() {
                return this.value;
            }

        @Override
        public String toString() {
            return "ClassData[" +
                    "pck=" + pck + ", " +
                    "sup=" + sup + ", " +
                    "target=" + target + ", " +
                    "preview=" + preview + ']';
        }

        }

    public static <T extends LoxClass> Supplier<LoxClass> register(Package pck, String name, Supplier<T> sup, Class<?> target) {
        PreviewClass preview = new PreviewClass(name, false);
        ClassData<T> targetData = new ClassData<>(pck, sup, target, preview);
        data.add(targetData);
        pck.addClass(name, preview);
        return targetData;
    }

    public static <T extends LoxClass> Supplier<LoxClass> register(Package pck, String name, Supplier<T> sup) {
        return register(pck, name, sup, null);
    }

    private static <T extends LoxClass> Supplier<LoxClass> registerMain(Supplier<T> sup, String name) {
        return register(langRoot, name, sup);
    }

    private static <T extends LoxClass> Supplier<LoxClass> registerMain(Supplier<T> sup, String name, Class<?> target) {
        return register(langRoot, name, sup, target);
    }

    public static LoxClass getClassForName(String type) {
        int arrayCount = 0;
        while (type.charAt(0) == '[') {
            arrayCount++;
            type = type.substring(1);
        }
        String[] packages = type.split("\\.");
        Package pg = rootPackage();
        for (int i = 0; i < packages.length; i++) {
            String name = packages[i];
            if (i == packages.length - 1) {
                String[] subClasses = name.split("\\$");
                if (!pg.hasClass(subClasses[0])) return null;
                LoxClass loxClass = pg.getClass(subClasses[0]);
                for (int j = 1; j < subClasses.length; j++) {
                    if (!loxClass.hasEnclosing(subClasses[j])) return null;
                    loxClass = loxClass.getEnclosing(subClasses[j]);
                }
                while (arrayCount > 0) {
                    loxClass = loxClass.array();
                    arrayCount--;
                }
                return loxClass;
            } else {
                if (!pg.hasPackage(name)) return null;
                pg = pg.getPackage(name);
            }
        }
        return null;
    }

    public static Package rootPackage() {
        return root;
    }

    public static boolean hasPackage(String pckName) {
        return root.hasPackage(pckName);
    }

    /**
     * gets a package
     * @param s the package, use "." to split
     * @return the package, or null if it doesn't exist
     */
    public static Package getPackage(String s) {
        String[] packages = s.split("\\.");
        Package p = rootPackage();
        for (String pck : packages) {
            p = p.getPackage(pck);
            if (p == null) break;
        }
        return p;
    }

    public static Package getOrCreatePackage(String s) {
        String[] packages = s.split("\\.");
        Package p = rootPackage();
        for (String pck : packages) {
            p = p.getOrCreatePackage(pck);
        }
        return p;
    }

    public static LoxClass getClass(List<Token> s, BiConsumer<Token, String> error) {
        Package pg = rootPackage();
        for (int i = 0; i < s.size(); i++) {
            Token token = s.get(i);
            String lexeme = token.lexeme();
            if (i == s.size() - 1) {
                if (!pg.hasClass(lexeme)) {
                    error.accept(token, "unknown class '" + lexeme + "'");
                    return null;
                }
                return pg.getClass(lexeme);
            } else {
                if (!pg.hasPackage(lexeme)) {
                    error.accept(token, "unknown package '" + lexeme + "'");
                    return null;
                }
                pg = pg.getPackage(lexeme);
            }
        }
        return null;
    }

    public static LoxClass lookupClass(Class<?> aClass) {
        if (!classLookup.containsKey(aClass)) throw new RuntimeException("class not registered");
        return classLookup.get(aClass);
    }

    public static <T> ReflectiveClass<? super T> createOrGetLookup(Class<? super T> superclass) {
        if (!classLookup.containsKey(superclass)) {
            ReflectiveClass<? super T> loaded = new ReflectiveClass<>(superclass);
            classLookup.put(superclass, loaded);
        }
        return (ReflectiveClass<? super T>) classLookup.get(superclass);
    }

    public static void createNativeClass(Class<?> clazz) {
        //TODO complete

        if (clazz.isAnnotationPresent(NativeClass.class)) {
            NativeClass nativeClass = clazz.getAnnotation(NativeClass.class);
            String pck = nativeClass.pck();
            String name = nativeClass.name();
            List<NativeMethodImpl> list = new ArrayList<>();
            for (Method declaredMethod : clazz.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(NativeMethod.class)) {
                    NativeMethodImpl impl = new NativeMethodImpl(
                            Arrays.stream(declaredMethod.getParameterTypes()).map(VarTypeManager::lookupClass).toList(),
                            lookupClass(declaredMethod.getReturnType()),
                            Modifier.isFinal(declaredMethod.getModifiers()),
                            Modifier.isAbstract(declaredMethod.getModifiers())
                    ) {
                        @Override
                        public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
                            return null;
                        }
                    };
                }
            }
        } else System.err.printf("can not create native class for '%s': class not annotated with 'NativeClass'", clazz.getCanonicalName());
    }
}
