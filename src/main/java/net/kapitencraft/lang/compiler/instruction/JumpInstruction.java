package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;

public class JumpInstruction extends SimpleInstruction {
    private int index;

    public JumpInstruction(Opcode opcode) {
        super(opcode);
    }

    public void setTarget(int index) {
        this.index = index;
    }
}
