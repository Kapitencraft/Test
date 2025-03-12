package net.kapitencraft.lang.run;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.generic.GenericClassReference;
import net.kapitencraft.lang.holder.class_ref.RegistryClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.clazz.primitive.*;
import net.kapitencraft.lang.run.natives.NativeClassLoader;

import java.util.*;
import java.util.function.BiConsumer;

public class VarTypeManager {
    private static final List<RegistryClassReference> data = new ArrayList<>();
    private static final Package root = new Package("");
    private static final Package LANG_ROOT = getOrCreatePackage("scripted.lang");
    private static final Package ANNOTATION_PCK = LANG_ROOT.getOrCreatePackage("annotations");

    public static final PrimitiveClass NUMBER = new NumberClass();
    public static final PrimitiveClass INTEGER = new IntegerClass();
    public static final PrimitiveClass FLOAT = new FloatClass();
    public static final PrimitiveClass DOUBLE = new DoubleClass();
    public static final PrimitiveClass BOOLEAN = new BooleanClass();
    public static final PrimitiveClass CHAR = new CharacterClass();
    public static final PrimitiveClass VOID = new VoidClass();

    static {
        NativeClassLoader.load();
        loadClasses();
    }

    public static final ClassReference OBJECT = getMainClass("Object");

    public static final ClassReference ENUM = getMainClass("Enum");

    public static final ClassReference STRING = getMainClass("String");

    public static final ClassReference THROWABLE = getMainClass("Throwable");
    public static final ClassReference STACK_OVERFLOW_ERROR = getMainClass("StackOverflowError");
    public static final ClassReference UNKNOWN_ERROR = getMainClass("UnknownError");
    public static final ClassReference MISSING_VAR_ERROR = getMainClass("MissingVarError");
    public static final ClassReference ARITHMETIC_EXCEPTION = getMainClass("ArithmeticException");
    public static final ClassReference FUNCTION_CALL_ERROR = getMainClass("FunctionCallError");
    public static final ClassReference INDEX_OUT_OF_BOUNDS_EXCEPTION = getMainClass("IndexOutOfBoundsException");

    public static final ClassReference OVERRIDE = getMainClass("Override");
    public static final ClassReference RETENTION_POLICY = getAnnotationClass("RetentionPolicy");
    public static final ClassReference RETENTION = getAnnotationClass("Retention");


    private static void loadClasses() {
        data.forEach(RegistryClassReference::create);
    }

    public static ClassReference getClassForName(String type) {
        int arrayCount = 0;
        while (type.charAt(type.length() - 1 - arrayCount * 2) == '[') arrayCount++;
        type = type.substring(0, type.length() - arrayCount * 2);
        String[] packages = type.split("\\.");
        Package pg = rootPackage();
        for (int i = 0; i < packages.length; i++) {
            String name = packages[i];
            if (i == packages.length - 1) {
                ClassReference reference = pg.getClass(name.replace("[]", ""));
                if (reference == null) return null;
                for (; arrayCount > 0; arrayCount--) {
                    reference = reference.array();
                }
                return reference;
            } else {
                if (!pg.hasPackage(name)) return null;
                pg = pg.getPackage(name);
            }
        }
        return null;
    }

    public static ClassReference getClassOrError(String type) {
        if (type.startsWith("?")) {
            ClassReference lowerBound = null, upperBound = null;
            if (type.length() > 1) {
                if (type.substring(2).startsWith("extends")) {
                    lowerBound = getClassOrError(type.substring(10));
                } else if (type.substring(2).startsWith("super")) {
                    upperBound = getClassOrError(type.substring(8));
                }
            }
            return new GenericClassReference("?", lowerBound, upperBound);
        }
        return Objects.requireNonNull(getClassForName(type), "unknown class: " + type);
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

    public static ClassReference getClassOrError(List<Token> s, BiConsumer<Token, String> error) {
        Package pg = rootPackage();
        for (int i = 0; i < s.size() - 1; i++) {
            Token token = s.get(i);
            String lexeme = token.lexeme();
            if (!pg.hasPackage(lexeme)) {
                error.accept(token, "unknown package '" + lexeme + "'");
                return null;
            }
            pg = pg.getPackage(lexeme);
        }
        Token token = s.get(s.size()-1);
        String lexeme = token.lexeme();
        if (!pg.hasClass(lexeme)) {
            error.accept(token, "unknown class '" + lexeme + "'");
            return null;
        }
        return pg.getClass(lexeme);
    }

    public static SourceClassReference getOrCreateClass(List<Token> path) {
        Package pg = rootPackage();
        for (int i = 0; i < path.size() - 1; i++) {
            String pckName = path.get(i).lexeme();
            pg = pg.getOrCreatePackage(pckName);
        }
        Token last = path.get(path.size() - 1);
        return SourceClassReference.from(last, pg.getOrCreateClass(last.lexeme()));
    }

    private static ClassReference getMainClass(String name) {
        return LANG_ROOT.getClass(name);
    }

    private static ClassReference getAnnotationClass(String name) {
        return ANNOTATION_PCK.getClass(name);
    }

    public static ClassReference getOrCreateClass(String name, String pck) {
        String[] packages = pck.split("\\.");
        Package pg = rootPackage();
        for (String aPackage : packages) {
            pg = pg.getOrCreatePackage(aPackage);
        }
        return pg.getOrCreateClass(name);
    }

    public static ClassReference getClassFromObject(Object o) {
        if (o instanceof Integer) return INTEGER.reference();
        if (o instanceof Float) return FLOAT.reference();
        if (o instanceof Double) return DOUBLE.reference();
        if (o instanceof Boolean) return BOOLEAN.reference();
        if (o instanceof Character) return CHAR.reference();
        if (o instanceof ClassInstance cI) {
            return cI.getType().reference();
        }
        throw new IllegalArgumentException("could not parse object to class: " + o);
    }

    public static List<ClassReference> getArgsFromObjects(List<Object> objects) {
        return objects.stream().map(VarTypeManager::getClassFromObject).toList();
    }
}
