package net.kapitencraft.lang.bytecode.exe;

public enum Opcode {
    RETURN, THROW,
    NULL, TRUE, FALSE,
    DUP, DUP_X1, DUP_X2, DUP2,
    DUP2_X1, DUP2_X2,
    POP, POP_2,
    GET, GET_0, GET_1, GET_2,
    ASSIGN, ASSIGN_0, ASSIGN_1, ASSIGN_2,
    SLICE,
    ARRAY_LENGTH,
    IA_STORE, DA_STORE, CA_STORE, RA_STORE,
    IA_LOAD, DA_LOAD, CA_LOAD, RA_LOAD,
    I_M1, I_0, I_1, I_2, I_3, I_4, I_5,
    D_M1, D_1,
    I_CONST, D_CONST, S_CONST,
    INVOKE,
    CONCENTRATION, D2F,
    AND, XOR, OR, NOT,
    EQUAL,
    NEQUAL,
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
    JUMP, JUMP_IF_FALSE, SWITCH,
    GET_FIELD, GET_STATIC, PUT_FIELD, PUT_STATIC, NEW;

    public static Opcode byId(int offset) {
        return values()[offset];
    }
}
