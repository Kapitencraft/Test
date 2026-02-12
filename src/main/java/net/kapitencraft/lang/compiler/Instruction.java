package net.kapitencraft.lang.compiler;

import net.kapitencraft.lang.bytecode.exe.Opcode;

public class Instruction {
    private final Opcode opcode;

    public Instruction(Opcode opcode) {
        this.opcode = opcode;
    }
}
