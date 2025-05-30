package net.kapitencraft.lang.holder.token;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.kapitencraft.lang.holder.token.TokenTypeCategory.*;

public enum TokenType {
    EQUAL(EQUALITY), NEQUAL(EQUALITY),
    LEQUAL(COMPARATORS), GEQUAL(COMPARATORS), GREATER(COMPARATORS), LESSER(COMPARATORS),
    EOF, EOA,
    POW(POINT_ARITHMETIC, ARITHMETIC_BINARY), MUL(POINT_ARITHMETIC, ARITHMETIC_BINARY), DIV(POINT_ARITHMETIC, ARITHMETIC_BINARY), MOD(POINT_ARITHMETIC, ARITHMETIC_BINARY), ADD(LINE_ARITHMETIC, ARITHMETIC_BINARY), SUB(LINE_ARITHMETIC, ARITHMETIC_BINARY),
    NOT, AND(BOOL_BINARY, KEY_WORD), OR(BOOL_BINARY, KEY_WORD), XOR(BOOL_BINARY, KEY_WORD),
    NUM(PRIMITIVE), STR(PRIMITIVE),
    FALSE(KEY_WORD, PRIMITIVE), TRUE(KEY_WORD, PRIMITIVE),
    NULL(KEY_WORD, PRIMITIVE), NEW(KEY_WORD), INSTANCEOF(KEY_WORD),
    LAMBDA, AT,
    BRACKET_O, BRACKET_C, C_BRACKET_O, C_BRACKET_C, S_BRACKET_O, S_BRACKET_C, COMMA, DOT, COLON,
    QUESTION_MARK,
    ASSIGN,
    ADD_ASSIGN(OPERATION_ASSIGN), SUB_ASSIGN(OPERATION_ASSIGN), POW_ASSIGN(OPERATION_ASSIGN), MUL_ASSIGN(OPERATION_ASSIGN), DIV_ASSIGN(OPERATION_ASSIGN), MOD_ASSIGN(OPERATION_ASSIGN),
    AND_ASSIGN(OPERATION_ASSIGN), OR_ASSIGN(OPERATION_ASSIGN), XOR_ASSIGN(OPERATION_ASSIGN),
    GROW, SHRINK,
    CLASS(KEY_WORD), INTERFACE(KEY_WORD), ENUM(KEY_WORD), ANNOTATION(KEY_WORD),
    FUNC(KEY_WORD), EXTENDS(KEY_WORD), IMPLEMENTS(KEY_WORD), IMPORT(KEY_WORD), PACKAGE(KEY_WORD), AS(KEY_WORD),
    FINAL(KEY_WORD), STATIC(KEY_WORD), ABSTRACT(KEY_WORD),
    SWITCH(KEY_WORD), CASE(KEY_WORD), DEFAULT(KEY_WORD),
    IF(KEY_WORD), ELSE(KEY_WORD), ELIF(KEY_WORD),
    FOR(KEY_WORD), WHILE(KEY_WORD),
    CONTINUE(KEY_WORD), BREAK(KEY_WORD),
    TRY(KEY_WORD), CATCH(KEY_WORD), FINALLY(KEY_WORD), SINGLE_OR,
    RETURN(KEY_WORD), THROW(KEY_WORD),
    THIS(KEY_WORD), SUPER(KEY_WORD),
    IDENTIFIER;

    private final List<TokenTypeCategory> categories;

    TokenType(TokenTypeCategory... categories) {
        this.categories = List.of(categories);
    }

    public boolean isCategory(TokenTypeCategory category) {
        return categories.contains(category);
    }

    public String id() {
        return name().toLowerCase();
    }

    public static TokenType readFromSubObject(JsonObject object, String type) {
        return TokenType.valueOf(object.getAsJsonPrimitive(type).getAsString().toUpperCase());
    }
}
