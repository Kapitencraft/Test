package net.kapitencraft.lang.bytecode.exe;

public enum Opcode {
    RETURN, THROW,
    NULL, TRUE, FALSE,
    I_CONST, D_CONST, S_CONST,
    CONCENTRATION,
    AND, XOR, OR, NOT,
    I_NEGATION, D_NEGATION,
    I_ADD, D_ADD,
    I_SUB, D_SUB,
    I_MUL, D_MUL,
    I_DIV, D_DIV,
    JUMP, JUMP_IF_FALSE;

    public static Opcode byId(int offset) {
        return values()[offset];
    }
}
