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
            case RETURN, THROW,
                 I_NEGATION, I_ADD, I_SUB, I_MUL, I_DIV,
                 D_NEGATION, D_ADD, D_SUB, D_MUL, D_DIV,
                 NULL, TRUE, FALSE, AND, XOR, OR, NOT, CONCENTRATION, I_1, D_1, I_M1, D_M1, D2F,
                 EQUAL, NEQUAL, I_LESSER, D_LESSER, I_GREATER, D_GREATER, I_LEQUAL, D_LEQUAL, I_GEQUAL, D_GEQUAL,
                 I_POW, D_POW, IA_STORE, DA_STORE, RA_STORE, IA_LOAD, DA_LOAD, RA_LOAD
                    -> simpleInstruction(opcode, offset);
            case GET, ASSIGN -> var(opcode, chunk, offset);
            case SLICE -> 0;
            case I_CONST -> intConstInstruction(opcode, chunk, offset);
            case D_CONST -> doubleConstInstruction(opcode, chunk, offset);
            case S_CONST -> stringConstInstruction(opcode, chunk, offset);
            case INVOKE -> invoke(chunk, offset);
            case JUMP, JUMP_IF_FALSE -> jump(opcode, chunk, offset);
            case SWITCH -> 0;
            case GET_FIELD -> 0;
            case GET_STATIC -> 0;
            case PUT_FIELD -> 0;
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
        return offset + 2;
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
        return offset + 2;
    }

    private static int simpleInstruction(Opcode opcode, int offset) {
        System.out.println(opcode.name());
        return offset + 1;
    }

    private static int read2b(byte[] code, int index) {
        return ((code[index++] << 8) & 255) | (code[index] & 255);
    }
}
