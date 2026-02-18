package net.kapitencraft.lang.compiler.instruction;

public class JumpTargetInstruction implements Instruction {
    private final int index;

    public JumpTargetInstruction(int index) {
        this.index = index;
    }
}