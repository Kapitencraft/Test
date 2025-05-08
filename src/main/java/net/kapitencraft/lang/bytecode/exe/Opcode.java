package net.kapitencraft.lang.bytecode.exe;

public enum Opcode {
    RETURN, THROW,
    NULL, TRUE, FALSE,
    GET, ASSIGN, SLICE,
    I_1, D_1,
    I_CONST, D_CONST, S_CONST,
    CONCENTRATION,
    AND, XOR, OR, NOT,
    EQUAL, NEQUAL,
    I_LESSER, D_LESSER,
    I_GREATER, D_GREATER,
    I_LEQUAL, D_LEQUAL,
    I_GEQUAL, D_GEQUAL,
    I_NEGATION, D_NEGATION,
    I_ADD, D_ADD,
    I_SUB, D_SUB,
    I_MUL, D_MUL,
    I_DIV, D_DIV,
    I_POW, D_POW,
    JUMP, JUMP_IF_FALSE;

    public static Opcode byId(int offset) {
        return values()[offset];
    }
}
