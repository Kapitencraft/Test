package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.storage.Chunk;
import net.kapitencraft.lang.compiler.ByteCodeBuilder;

public class JumpTargetInstruction implements Instruction {
    //must be added AFTER the target instruction

    private final int index;

    public JumpTargetInstruction(int index) {
        this.index = index;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {

    }
}