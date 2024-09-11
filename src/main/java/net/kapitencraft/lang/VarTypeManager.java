package net.kapitencraft.lang;

import net.kapitencraft.lang.ast.Token;
import net.kapitencraft.lang.ast.TokenType;

import java.util.HashMap;
import java.util.Map;

import static net.kapitencraft.lang.ast.TokenType.*;

public class VarTypeManager {
    private static final Map<String, TokenType> keywords = new HashMap<>();

    private static final Map<Class<?>, String> nameForClass = new HashMap<>();
    private static final Map<String, Class<?>> classForName = new HashMap<>();

    static {
        keywords.put("class",    CLASS);
        keywords.put("false",    FALSE);
        keywords.put("true",     TRUE);
        keywords.put("and",      AND);
        keywords.put("or",       OR);
        keywords.put("xor",      XOR);
        keywords.put("for",      FOR);
        keywords.put("def",      FUNC);
        keywords.put("if",       IF);
        keywords.put("else",     ELSE);
        keywords.put("elif",     ELIF);
        keywords.put("null",     NULL);
        keywords.put("return",   RETURN);
        keywords.put("super",    SUPER);
        keywords.put("this",     THIS);
        keywords.put("var",      VAR);
        keywords.put("while",    WHILE);
        keywords.put("break",    BREAK);
        keywords.put("continue", CONTINUE);
        initialize();
    }

    private static void initialize() {
        register("int", Integer.class);
        register("bool", Boolean.class);
        register("double", Double.class);
        register("char", Character.class);
        register("String", String.class);
    }

    public static void register(String name, Class<?> clazz) {
        nameForClass.put(clazz, name);
        classForName.put(name, clazz);
    }

    public static TokenType getType(String name) {
        if (keywords.containsKey(name)) return keywords.get(name);
        if (classForName.containsKey(name)) return VAR_TYPE;
        return IDENTIFIER;
    }

    public static Class<?> getClassForName(String type) {
        return classForName.get(type);
    }
}
