package net.kapitencraft.lang;

import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.LoxClass;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.oop.PrimitiveClass;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.util.HashMap;
import java.util.Map;

import static net.kapitencraft.lang.holder.token.TokenType.*;

public class VarTypeManager {
    private static final Package root = new Package();
    private static final Map<String, TokenType> keywords = new HashMap<>();

    public static final LoxClass NUMBER = new PrimitiveClass("num");
    public static final LoxClass INTEGER = new PrimitiveClass(NUMBER, "int");
    public static final LoxClass FLOAT = new PrimitiveClass(NUMBER, "float");
    public static final LoxClass DOUBLE = new PrimitiveClass(NUMBER, "double");
    public static final LoxClass BOOLEAN = new PrimitiveClass("bool");
    public static final LoxClass CHAR = new PrimitiveClass("char");
    public static final LoxClass STRING = new PrimitiveClass("string");

    public static final LoxClass VOID = new PrimitiveClass("void");
    public static final LoxClass OBJECT = new PrimitiveClass("Object");

    static {
        keywords.put("class",    CLASS);
        keywords.put("extends",  EXTENDS);
        keywords.put("false",    FALSE);
        keywords.put("true",     TRUE);
        keywords.put("and",      AND);
        keywords.put("or",       OR);
        keywords.put("xor",      XOR);
        keywords.put("for",      FOR);
        keywords.put("def",      FUNC);
        keywords.put("final",    FINAL);
        keywords.put("static",   STATIC); //TOO implement static
        keywords.put("if",       IF);
        keywords.put("else",     ELSE);
        keywords.put("elif",     ELIF);
        keywords.put("null",     NULL);
        keywords.put("return",   RETURN);
        keywords.put("super",    SUPER);
        keywords.put("this",     THIS);
        keywords.put("while",    WHILE);
        keywords.put("break",    BREAK);
        keywords.put("continue", CONTINUE);
        keywords.put("switch",   SWITCH);
        keywords.put("case",     CASE);
        initialize();
    }

    private static void initialize() {
        registerMain("int", INTEGER);
        registerMain("float", FLOAT);
        registerMain("bool", BOOLEAN);
        registerMain("double", DOUBLE);
        registerMain("char", CHAR);
        registerMain("String", STRING);
    }

    public static void registerMain(String name, LoxClass clazz) {
        root.getOrCreatePackage("scripted").getOrCreatePackage("lang").addClass(name, clazz);
    }

    public static TokenType getType(String name) {
        if (keywords.containsKey(name)) return keywords.get(name);

        return IDENTIFIER;
    }

    public static LoxClass getClassForName(String type) {
        String[] packages = type.split("\\.");
        Package pg = rootPackage();
        for (int i = 0; i < packages.length; i++) {
            String lexeme = packages[i];
            if (i == packages.length - 1) {
                if (!pg.hasClass(lexeme)) {
                    return null;
                }
                return pg.getClass(lexeme);
            } else {
                if (!pg.hasPackage(lexeme)) {
                    return null;
                }
                pg = pg.getPackage(lexeme);
            }
        }
        return null;
    }

    public static Package rootPackage() {
        return root;
    }
}
