package net.kapitencraft.lang.run;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.RegistryClassReference;
import net.kapitencraft.lang.natives.scripted.lang.*;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.natives.scripted.lang.IndexOutOfBoundsException;
import net.kapitencraft.lang.natives.scripted.lang.SystemClass;
import net.kapitencraft.lang.natives.scripted.lang.annotation.OverrideAnnotation;
import net.kapitencraft.lang.oop.clazz.ClassType;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.oop.clazz.primitive.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class VarTypeManager {
    private static final List<RegistryClassReference> data = new ArrayList<>();
    private static final Package root = new Package("");
    private static final Package langRoot = getOrCreatePackage("scripted.lang");

    public static final ScriptedClass NUMBER = new NumberClass();
    public static final ScriptedClass INTEGER = new IntegerClass();
    public static final ScriptedClass FLOAT = new FloatClass();
    public static final ScriptedClass DOUBLE = new DoubleClass();
    public static final ScriptedClass BOOLEAN = new BooleanClass();
    public static final ScriptedClass CHAR = new CharacterClass();
    public static final ScriptedClass VOID = new VoidClass();

    public static final ClassReference OBJECT = registerMain(ClassType.CLASS, ObjectClass::new, "Object");

    public static final ClassReference ENUM = registerMain(ClassType.CLASS, EnumClass::new, "Enum");

    public static final ClassReference STRING = registerMain(StringClass::new, "String", ClassType.CLASS, String.class);

    public static final ClassReference THROWABLE = registerMain(ClassType.CLASS, () -> new ThrowableClass("Throwable", "scripted.lang"), "Throwable");
    public static final ClassReference STACK_OVERFLOW_EXCEPTION = registerMain(ClassType.CLASS, StackOverflowExceptionClass::new, "StackOverflowException");
    public static final ClassReference MISSING_VAR_EXCEPTION = registerMain(ClassType.CLASS, MissingVarExceptionClass::new, "MissingVarException");
    public static final ClassReference ARITHMETIC_EXCEPTION = registerMain(ClassType.CLASS, ArithmeticExceptionClass::new, "ArithmeticException");
    public static final ClassReference FUNCTION_CALL_ERROR = registerMain(ClassType.CLASS, FunctionCallErrorClass::new, "FunctionCallError");
    public static final ClassReference INDEX_OUT_OF_BOUNDS_EXCEPTION = registerMain(ClassType.CLASS, IndexOutOfBoundsException::new, "IndexOutOfBoundsException");

    public static final ClassReference SYSTEM = registerMain(ClassType.CLASS, SystemClass::new, "System");
    public static final ClassReference MATH = registerMain(ClassType.CLASS, MathClass::new, "Math");

    public static final ClassReference OVERRIDE = registerMain(ClassType.ANNOTATION, OverrideAnnotation::new, "Override");

    static {
        loadClasses();
    }

    private static void loadClasses() {
        data.forEach(RegistryClassReference::create);
    }

    public static ClassReference register(Package pck, String name, ClassType type, Supplier<ScriptedClass> sup, Class<?> target) {
        RegistryClassReference classReference = new RegistryClassReference(name, pck.getName(), type, sup);
        data.add(classReference);
        pck.addClass(name, classReference);
        return classReference;
    }

    public static ClassReference register(Package pck, String name, ClassType type, Supplier<ScriptedClass> sup) {
        return register(pck, name, type, sup, null);
    }

    private static ClassReference registerMain(ClassType type, Supplier<ScriptedClass> sup, String name) {
        return register(langRoot, name, type, sup);
    }

    private static ClassReference registerMain(Supplier<ScriptedClass> sup, String name, ClassType type, Class<?> target) {
        return register(langRoot, name, type, sup, target);
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
                String[] subClasses = name.split("\\$");
                ClassReference loxClass = pg.getClass(subClasses[0].replace("[]", ""));
                if (loxClass == null) return null; //TODO fix enclosed classes not working exposed
                for (int j = 1; j < subClasses.length; j++) {
                    if (!loxClass.get().hasEnclosing(subClasses[j])) return null;
                    loxClass = loxClass.get().getEnclosing(subClasses[j]);
                }
                for (; arrayCount > 0; arrayCount--) {
                    loxClass = loxClass.array();
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

    public static ClassReference getClass(List<Token> s, BiConsumer<Token, String> error) {
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
}
