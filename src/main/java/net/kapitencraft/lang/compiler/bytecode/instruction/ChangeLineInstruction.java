package net.kapitencraft.lang.compiler.bytecode.instruction;

import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.compiler.bytecode.ByteCodeBuilder;

public class ChangeLineInstruction implements Instruction {
    private final int line;

    public ChangeLineInstruction(int line) {
        this.line = line;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.changeLineIfNecessary(line);
    }
}
