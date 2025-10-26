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
                 I_NEGATION, I_ADD, I_SUB, I_MUL, I_DIV, I_POW,
                 D_NEGATION, D_ADD, D_SUB, D_MUL, D_DIV, D_POW,
                 F_NEGATION, F_ADD, F_SUB, F_MUL, F_DIV, F_POW,
                 ASSIGN_0, ASSIGN_1, ASSIGN_2,
                 GET_0, GET_1, GET_2,
                 NULL, TRUE, FALSE, AND, XOR, OR, NOT, CONCENTRATION,
                 I_M1, I_0, I_1, I_2, I_3, I_4, I_5,
                 D_M1, D_1,
                 F_M1, F_1,
                 D2F, ARRAY_LENGTH,
                 EQUAL, NEQUAL,
                 I_LESSER, D_LESSER, F_LESSER,
                 I_GREATER, D_GREATER, F_GREATER,
                 I_LEQUAL, D_LEQUAL, F_LEQUAL,
                 I_GEQUAL, D_GEQUAL, F_GEQUAL,
                 IA_STORE, DA_STORE, CA_STORE, FA_STORE, RA_STORE,
                 IA_LOAD, DA_LOAD, CA_LOAD, FA_LOAD, RA_LOAD,
                 SLICE, CA_NEW, DA_NEW, FA_NEW, IA_NEW
                    -> simpleInstruction(opcode, offset);
            case GET, ASSIGN -> var(opcode, chunk, offset);
            case I_CONST -> intConstInstruction(opcode, chunk, offset);
            case D_CONST -> doubleConstInstruction(opcode, chunk, offset);
            case F_CONST -> floatConstInstruction(opcode, chunk, offset);
            case NEW, S_CONST -> stringConstInstruction(opcode, chunk, offset);
            case INVOKE_STATIC, INVOKE_VIRTUAL -> invoke(opcode, chunk, offset);
            case JUMP, JUMP_IF_FALSE -> jump(opcode, chunk, offset);
            case SWITCH -> 0;
            case GET_FIELD, PUT_FIELD -> fieldOp(opcode, chunk, offset);
            case GET_STATIC, PUT_STATIC -> staticFieldOp(opcode, chunk, offset);
            case RA_NEW -> newArray(opcode, chunk, offset);
        };
    }

    private static int newArray(Opcode opcode, Chunk chunk, int offset) {
        int pos = read2b(chunk.code(), offset + 1);
        String string = VirtualMachine.constString(chunk.constants(), pos);
        System.out.printf("%-16s %s\n", opcode, string);
        return offset + 3;
    }

    private static int jump(Opcode opcode, Chunk chunk, int offset) {
        int jump = read2b(chunk.code(), offset + 1);
        System.out.printf("%-16s %4d -> %d\n", opcode, offset, jump);
        return offset + 3;
    }

    private static int var(Opcode opcode, Chunk chunk, int offset) {
        System.out.printf("%-16s %4d\n", opcode, chunk.code()[offset + 1]);
        return offset + 2;
    }

    private static int invoke(Opcode opcode, Chunk chunk, int offset) {
        int id = read2b(chunk.code(), offset + 1);
        String signature = VirtualMachine.constString(chunk.constants(), id);
        System.out.printf("%-16s %4d '%s'\n", opcode, id, signature);
        return offset + 3;
    }

    private static int fieldOp(Opcode opcode, Chunk chunk, int offset) {
        int id = read2b(chunk.code(), offset + 1);
        String name = VirtualMachine.constString(chunk.constants(), id);
        System.out.printf("%-16s %4d '%s'\n", opcode, id, name);
        return offset + 3;
    }

    private static int staticFieldOp(Opcode opcode, Chunk chunk, int offset) {
        int id = read2b(chunk.code(), offset + 1);
        String c = VirtualMachine.constString(chunk.constants(), id);
        int nameId = read2b(chunk.code(), offset + 3);
        String name = VirtualMachine.constString(chunk.constants(), nameId);
        System.out.printf("%-16s %4d '%s': '%s'\n", opcode, id, c, name);
        return offset + 5;
    }

    private static int stringConstInstruction(Opcode opcode, Chunk chunk, int offset) {
        return constInstruction(opcode, chunk, offset, VirtualMachine::constString);
    }

    private static int doubleConstInstruction(Opcode opcode, Chunk chunk, int offset) {
        return constInstruction(opcode, chunk, offset, VirtualMachine::constDouble);
    }

    private static int floatConstInstruction(Opcode opcode, Chunk chunk, int offset) {
        return constInstruction(opcode, chunk, offset, VirtualMachine::constFloat);
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
