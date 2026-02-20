package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.bytecode.storage.Chunk;

public class LocalInstruction extends SimpleInstruction {
    private final int id;

    public LocalInstruction(Opcode opcode, int id) {
        super(opcode);
        this.id = id;
    }

    @Override
    public void save(Chunk.Builder builder, int[] instStartIndexes) {
        super.save(builder, instStartIndexes);
        builder.addArg(id);
    }
}
