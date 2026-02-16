package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;

public class JumpInstruction extends Instruction {
    private int index;

    public JumpInstruction(Opcode opcode) {
        super(opcode);
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
