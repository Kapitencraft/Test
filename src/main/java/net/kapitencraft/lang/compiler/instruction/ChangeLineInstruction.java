package net.kapitencraft.lang.compiler.instruction;

public class ChangeLineInstruction implements Instruction {
    private final int line;

    public ChangeLineInstruction(int line) {
        this.line = line;
    }
}
