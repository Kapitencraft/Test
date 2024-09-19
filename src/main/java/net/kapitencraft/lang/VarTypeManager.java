package net.kapitencraft.lang;

import com.sun.jdi.VirtualMachine;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.LoxClass;
import net.kapitencraft.lang.oop.Package;
import net.kapitencraft.lang.oop.PrimitiveClass;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

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
    public static final LoxClass STRING = new PrimitiveClass("String");

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
        keywords.put("static",   STATIC); //TODO implement static
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
        registerMain(OBJECT);
        registerMain(NUMBER);
        registerMain(INTEGER);
        registerMain(FLOAT);
        registerMain(BOOLEAN);
        registerMain(DOUBLE);
        registerMain(CHAR);
        registerMain(STRING);
        registerMain(VOID);
    }

    public static void registerMain(LoxClass clazz) {
        root.getOrCreatePackage("scripted").getOrCreatePackage("lang").addClass(clazz.name(), clazz);
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

    public static LoxClass getClass(List<Token> s, BiConsumer<Token, String> error) {
        Package pg = VarTypeManager.rootPackage();
        for (int i = 0; i < s.size(); i++) {
            Token token = s.get(i);
            String lexeme = token.lexeme;
            if (i == s.size() - 1) {
                if (!pg.hasClass(lexeme)) {
                    error.accept(token, "unknown symbol");
                    return null;
                }
                return pg.getClass(lexeme);
            } else {
                if (!pg.hasPackage(lexeme)) {
                    error.accept(token, "unknown symbol");
                    return null;
                }
                pg = pg.getPackage(lexeme);
            }
        }
        return null;
    }
}
