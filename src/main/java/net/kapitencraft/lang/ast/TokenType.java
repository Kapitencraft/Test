package net.kapitencraft.lang.ast;

public enum TokenType {
    EQUAL, NEQUAL,
    LEQUAL, GEQUAL, GREATER, LESSER,
    EOF, EOA,
    MUL, DIV, MOD, ADD, SUB,
    NOT, AND, OR, XOR,
    NUM, STR,
    FALSE, TRUE,
    VAR_TYPE,
    NULL,
    LAMBDA,
    BRACKET_O, BRACKET_C, C_BRACKET_O, C_BRACKET_C, COMMA, DOT,
    ASSIGN,
    ADD_ASSIGN, SUB_ASSIGN, MUL_ASSIGN, DIV_ASSIGN, MOD_ASSIGN,
    GROW, SHRINK,
    CLASS, FUNC,
    VAR,
    FOR, WHILE,
    CONTINUE, BREAK,
    RETURN,
    IF, ELSE, ELIF,
    IN_LINE,
    THIS, SUPER,
    IDENTIFIER
}
