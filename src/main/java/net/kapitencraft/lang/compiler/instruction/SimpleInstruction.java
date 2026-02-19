package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.bytecode.storage.Chunk;

public class SimpleInstruction implements Instruction {
    private final Opcode opcode;

    public SimpleInstruction(Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public void save(Chunk.Builder builder) {
        builder.addCode(opcode);
    }
}
