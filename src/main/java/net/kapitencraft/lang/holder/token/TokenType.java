package net.kapitencraft.lang.holder.token;

import java.util.List;

public enum TokenType {
    EQUAL(TokenTypeCategory.COMPARATORS), NEQUAL(TokenTypeCategory.COMPARATORS),
    LEQUAL(TokenTypeCategory.COMPARATORS), GEQUAL(TokenTypeCategory.COMPARATORS), GREATER(TokenTypeCategory.COMPARATORS), LESSER(TokenTypeCategory.COMPARATORS),
    EOF, EOA,
    MUL(TokenTypeCategory.ARITHMETIC_BINARY), DIV(TokenTypeCategory.ARITHMETIC_BINARY), MOD(TokenTypeCategory.ARITHMETIC_BINARY), ADD(TokenTypeCategory.ARITHMETIC_BINARY), SUB(TokenTypeCategory.ARITHMETIC_BINARY),
    NOT, AND(TokenTypeCategory.BOOL_BINARY), OR(TokenTypeCategory.BOOL_BINARY), XOR(TokenTypeCategory.BOOL_BINARY),
    NUM, STR,
    FALSE, TRUE,
    VAR_TYPE,
    NULL,
    LAMBDA,
    BRACKET_O, BRACKET_C, C_BRACKET_O, C_BRACKET_C, COMMA, DOT,
    WHEN_CONDITION, WHEN_FALSE,
    ASSIGN,
    ADD_ASSIGN(TokenTypeCategory.OPERATION_ASSIGN), SUB_ASSIGN(TokenTypeCategory.OPERATION_ASSIGN), MUL_ASSIGN(TokenTypeCategory.OPERATION_ASSIGN), DIV_ASSIGN(TokenTypeCategory.OPERATION_ASSIGN), MOD_ASSIGN(TokenTypeCategory.OPERATION_ASSIGN),
    GROW, SHRINK,
    CLASS, FUNC,
    SWITCH, CASE, DEFAULT,
    IF, ELSE, ELIF,
    FOR, WHILE,
    CONTINUE, BREAK,
    RETURN,
    IN_LINE,
    THIS, SUPER,
    IDENTIFIER;

    public final List<TokenTypeCategory> categories;

    TokenType(TokenTypeCategory... categories) {
        this.categories = List.of(categories);
    }
}
