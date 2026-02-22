package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.compiler.ByteCodeBuilder;

public interface Instruction {

    void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips);

    default int length() {
        return -1; //return -1 for non-code instructions
    }
}
