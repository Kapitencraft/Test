package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.bytecode.storage.Chunk;

public class StringArgInstruction extends SimpleInstruction {
    private final String value;

    public StringArgInstruction(Opcode opcode, String value) {
        super(opcode);
        this.value = value;
    }

    @Override
    public void save(Chunk.Builder builder) {
        super.save(builder);
        builder.injectString(value);
    }
}
