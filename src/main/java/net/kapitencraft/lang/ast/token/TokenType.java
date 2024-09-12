package net.kapitencraft.lang.ast.token;

import java.util.List;
import static net.kapitencraft.lang.ast.token.TokenTypeCategory.*;

public enum TokenType {
    EQUAL, NEQUAL,
    LEQUAL, GEQUAL, GREATER, LESSER,
    EOF, EOA,
    MUL(ARITHMETIC_BINARY), DIV(ARITHMETIC_BINARY), MOD(ARITHMETIC_BINARY), ADD(ARITHMETIC_BINARY), SUB(ARITHMETIC_BINARY),
    NOT(BOOL_BINARY), AND(BOOL_BINARY), OR(BOOL_BINARY), XOR(BOOL_BINARY),
    NUM, STR,
    FALSE, TRUE,
    VAR_TYPE,
    NULL,
    LAMBDA,
    BRACKET_O, BRACKET_C, C_BRACKET_O, C_BRACKET_C, COMMA, DOT,
    ASSIGN,
    ADD_ASSIGN(OPERATION_ASSIGN), SUB_ASSIGN(OPERATION_ASSIGN), MUL_ASSIGN(OPERATION_ASSIGN), DIV_ASSIGN(OPERATION_ASSIGN), MOD_ASSIGN(OPERATION_ASSIGN),
    GROW, SHRINK,
    CLASS, FUNC,
    FOR, WHILE,
    CONTINUE, BREAK,
    RETURN,
    IF, ELSE, ELIF,
    IN_LINE,
    THIS, SUPER,
    IDENTIFIER;

    public final List<TokenTypeCategory> categories;

    TokenType(TokenTypeCategory... categories) {
        this.categories = List.of(categories);
    }
}
