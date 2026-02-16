package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;

public class JumpTargetInstruction extends Instruction {
    private final int index;

    public JumpTargetInstruction(int index) {
        super(Opcode.JUMP);
        this.index = index;
    }
}
