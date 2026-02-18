package net.kapitencraft.lang.compiler.instruction;

import java.util.List;

public class JumpMultiTargetInstruction implements Instruction {
    private final List<Integer> origins;

    public JumpMultiTargetInstruction(List<Integer> origins) {
        this.origins = origins;
    }
}
