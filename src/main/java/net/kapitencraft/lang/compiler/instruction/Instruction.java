package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.storage.Chunk;

public interface Instruction {

    void save(Chunk.Builder builder, int[] instStartIndexes);

    default int length() {
        return 0;
    }
}
