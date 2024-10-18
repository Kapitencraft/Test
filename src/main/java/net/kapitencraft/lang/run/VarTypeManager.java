package net.kapitencraft.lang.run;

import net.kapitencraft.lang.natives.scripted.lang.*;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.token.TokenTypeCategory;
import net.kapitencraft.lang.natives.scripted.util.SystemClass;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.oop.clazz.ReflectiveClass;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.kapitencraft.lang.holder.token.TokenType.*;

public class VarTypeManager {
    private static final Package root = new Package("");
    private static final Map<Class<?>, LoxClass> classLookup = new HashMap<>();
    public static final ReflectiveLoader reflectiveLoader = new ReflectiveLoader();
    private static final Map<String, TokenType> keywords;

    public static final LoxClass OBJECT = new PrimitiveClass("Object", null);

    public static final LoxClass NUMBER = new PrimitiveClass("num" , null);
    public static final LoxClass INTEGER = new PrimitiveClass(NUMBER, "int", 0);
    public static final LoxClass FLOAT = new PrimitiveClass(NUMBER, "float", 0f);
    public static final LoxClass DOUBLE = new PrimitiveClass(NUMBER, "double", 0d);
    public static final LoxClass BOOLEAN = new PrimitiveClass("bool", false);
    public static final LoxClass CHAR = new PrimitiveClass("char", ' ');
    public static final LoxClass STRING = new PrimitiveClass("String", "");


    public static final LoxClass VOID = new PrimitiveClass("void", null);
    //TODO move Object away from primitive class given that it isn't actually one

    public static final LoxClass THROWABLE = new ThrowableClass();
    public static final LoxClass STACK_OVERFLOW_EXCEPTION = new StackOverflowExceptionClass();
    public static final LoxClass MISSING_VAR_EXCEPTION = new MissingVarExceptionClass();
    public static final LoxClass ARITHMETIC_EXCEPTION = new ArithmeticExceptionClass();
    public static final LoxClass FUNCTION_CALL_ERROR = new FunctionCallError();

    public static final LoxClass SYSTEM = new SystemClass();

    static {
        keywords = Arrays.stream(values()).filter(tokenType -> tokenType.isCategory(TokenTypeCategory.KEY_WORD)).collect(Collectors.toMap(tokenType -> tokenType.name().toLowerCase(Locale.ROOT), Function.identity()));
        initialize();
    }

    private static void initialize() {
        registerMain(OBJECT);
        registerMain(NUMBER);
        registerMain(INTEGER);
        registerMain(FLOAT);
        registerMain(BOOLEAN);
        registerMain(DOUBLE);
        registerMain(CHAR);
        registerMain(STRING);
        registerMain(VOID);
        registerMain(THROWABLE);
        registerMain(STACK_OVERFLOW_EXCEPTION);
        registerMain(MISSING_VAR_EXCEPTION);
        registerMain(ARITHMETIC_EXCEPTION);
        registerMain(FUNCTION_CALL_ERROR);
        registerMain(SYSTEM);
        classLookup.put(Number.class, NUMBER);
        classLookup.put(Integer.class, INTEGER);
        classLookup.put(int.class, INTEGER);
        classLookup.put(Double.class, DOUBLE);
        classLookup.put(double.class, DOUBLE);
        classLookup.put(Float.class, FLOAT);
        classLookup.put(float.class, FLOAT);
        classLookup.put(Boolean.class, BOOLEAN);
        classLookup.put(boolean.class, BOOLEAN);
        classLookup.put(Void.class, VOID);
        classLookup.put(void.class, VOID);
        classLookup.put(Object.class, OBJECT);
        classLookup.put(String.class, STRING);

    }

    private static void registerMain(LoxClass clazz) {
        getOrCreatePackage("scripted.lang").addClass(clazz.name(), clazz);
    }

    private static void registerReflectiveMain(Class<?> clazz) {
        getOrCreatePackage("scripted.lang").addClass(clazz);
    }

    public static TokenType getType(String name) {
        if (keywords.containsKey(name)) return keywords.get(name);

        return IDENTIFIER;
    }

    public static LoxClass getClassForName(String type) {
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
}
