package net.kapitencraft.lang.bytecode.exe;

import java.util.function.BiFunction;

public class Disassembler {

    public static void disassemble(Chunk chunk, String name) {
        System.out.printf("== %s ==\n", name);

        byte[] code = chunk.code();
        for (int offset = 0; offset < code.length;) {
            offset = disassembleInstruction(chunk, offset);
        }
    }

    private static int disassembleInstruction(Chunk chunk, int offset) {
        System.out.printf("%04d ", offset);

        Opcode opcode = Opcode.byId(chunk.code()[offset]);
        return switch (opcode) {
            case RETURN,
                 I_NEGATION, I_ADD, I_SUB, I_MUL, I_DIV,
                 D_NEGATION, D_ADD, D_SUB, D_MUL, D_DIV,
                 NULL, TRUE, FALSE, AND, XOR, OR, NOT, CONCENTRATION
                    -> simpleInstruction(opcode, offset);
            case I_CONST -> intConstInstruction(opcode, chunk, offset);
            case D_CONST -> doubleConstInstruction(opcode, chunk, offset);
            case S_CONST -> stringConstInstruction(opcode, chunk, offset);
        };
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
        System.out.printf("%-16s %4d '%s'\n", opcode.name(), chunk.code()[offset + 1], getter.apply(chunk.constants(), (int) chunk.code()[offset + 1]));
        return offset + 2;
    }

    private static int simpleInstruction(Opcode opcode, int offset) {
        System.out.println(opcode.name());
        return offset + 1;
    }
}
