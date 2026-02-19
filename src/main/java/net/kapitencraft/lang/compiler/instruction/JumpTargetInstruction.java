package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.storage.Chunk;

public class JumpTargetInstruction implements Instruction {
    private final int index;

    public JumpTargetInstruction(int index) {
        this.index = index;
    }

    @Override
    public void save(Chunk.Builder builder) {

    }
}