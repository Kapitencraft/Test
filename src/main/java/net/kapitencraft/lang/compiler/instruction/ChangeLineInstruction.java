package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.storage.Chunk;

public class ChangeLineInstruction implements Instruction {
    private final int line;

    public ChangeLineInstruction(int line) {
        this.line = line;
    }

    @Override
    public void save(Chunk.Builder builder) {
        builder.changeLineIfNecessary(line);
    }
}
