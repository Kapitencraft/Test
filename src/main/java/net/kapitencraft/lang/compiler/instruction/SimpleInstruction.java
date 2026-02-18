package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;

public class SimpleInstruction implements Instruction {
    private final Opcode opcode;

    public SimpleInstruction(Opcode opcode) {
        this.opcode = opcode;
    }
}
