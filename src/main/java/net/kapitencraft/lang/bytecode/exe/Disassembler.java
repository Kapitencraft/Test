package net.kapitencraft.lang.bytecode.exe;

import java.util.function.BiFunction;

public class Disassembler {

    @SuppressWarnings("StatementWithEmptyBody")
    public static void disassemble(Chunk chunk, String name) {
        System.out.printf("== %s ==\n", name);

        byte[] code = chunk.code();
        for (int offset = 0; offset < code.length; offset = disassembleInstruction(chunk, offset)) {
        }
    }

    private static int disassembleInstruction(Chunk chunk, int offset) {
        System.out.printf("%04d ", offset);

        Opcode opcode = Opcode.byId(chunk.code()[offset]);
        return switch (opcode) {
            case POP, POP_2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2,
                 RETURN, THROW,
                 I_NEGATION, I_ADD, I_SUB, I_MUL, I_DIV,
                 D_NEGATION, D_ADD, D_SUB, D_MUL, D_DIV,
                 ASSIGN_0, ASSIGN_1, ASSIGN_2,
                 GET_0, GET_1, GET_2,
                 NULL, TRUE, FALSE, AND, XOR, OR, NOT, CONCENTRATION,
                 I_M1, I_0, I_1, I_2, I_3, I_4, I_5,
                 D_M1, D_1,
                 D2F, ARRAY_LENGTH,
                 EQUAL, NEQUAL, I_LESSER, D_LESSER, I_GREATER, D_GREATER, I_LEQUAL, D_LEQUAL, I_GEQUAL, D_GEQUAL,
                 I_POW, D_POW, IA_STORE, DA_STORE, CA_STORE, RA_STORE, IA_LOAD, DA_LOAD, CA_LOAD, RA_LOAD, SLICE
                    -> simpleInstruction(opcode, offset);
            case GET, ASSIGN -> var(opcode, chunk, offset);
            case I_CONST -> intConstInstruction(opcode, chunk, offset);
            case D_CONST -> doubleConstInstruction(opcode, chunk, offset);
            case NEW, S_CONST -> stringConstInstruction(opcode, chunk, offset);
            case INVOKE -> invoke(chunk, offset);
            case JUMP, JUMP_IF_FALSE -> jump(opcode, chunk, offset);
            case SWITCH -> 0;
            case GET_FIELD, PUT_FIELD -> fieldOp(opcode, chunk, offset);
            case GET_STATIC -> 0;
            case PUT_STATIC -> 0;
        };
    }

    private static int jump(Opcode opcode, Chunk chunk, int offset) {
        int jump = (chunk.code()[offset + 1] << 8) | chunk.code()[offset + 2];
        System.out.printf("%-16s %4d -> %d\n", opcode, offset, jump);
        return offset + 3;
    }

    private static int var(Opcode opcode, Chunk chunk, int offset) {
        System.out.printf("%-16s %4d\n", opcode, chunk.code()[offset + 1]);
        return offset + 2;
    }

    private static int invoke(Chunk chunk, int offset) {
        int id = read2b(chunk.code(), offset + 1);
        String signature = VirtualMachine.constString(chunk.constants(), id);
        System.out.printf("%-16s %4d '%s'\n", "INVOKE", id, signature);
        return offset + 3;
    }

    private static int fieldOp(Opcode opcode, Chunk chunk, int offset) {
        int id = read2b(chunk.code(), offset + 1);
        String name = VirtualMachine.constString(chunk.constants(), id);
        System.out.printf("%-16s %4d '%s'\n", opcode, id, name);
        return offset + 3;
    }

    private static int stringConstInstruction(Opcode opcode, Chunk chunk, int offset) {
        return constInstruction(opcode, chunk, offset, VirtualMachine::constString);
    }

    private static int doubleConstInstruction(Opcode opcode, Chunk chunk, int offset) {
        return constInstruction(opcode, chunk, offset, VirtualMachine::constDouble);
    }

    private static int intConstInstruction(Opcode opcode, Chunk chunk, int offset) {
        return constInstruction(opcode, chunk, offset, VirtualMachine::constInt);
    }

    private static int constInstruction(Opcode opcode, Chunk chunk, int offset, BiFunction<byte[], Integer, Object> getter) {
        int pos = read2b(chunk.code(), offset + 1);
        System.out.printf("%-16s %4d '%s'\n", opcode.name(), pos, getter.apply(chunk.constants(), pos));
        return offset + 3;
    }

    private static int simpleInstruction(Opcode opcode, int offset) {
        System.out.println(opcode.name());
        return offset + 1;
    }

    private static int read2b(byte[] code, int index) {
        return ((code[index++] & 255) << 8) | (code[index] & 255);
    }
}
