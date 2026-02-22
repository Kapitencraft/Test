package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.compiler.ByteCodeBuilder;

import java.util.List;

public class JumpMultiTargetInstruction implements Instruction {
    private final List<Integer> origins;

    public JumpMultiTargetInstruction(List<Integer> origins) {
        this.origins = origins;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
    }
}
