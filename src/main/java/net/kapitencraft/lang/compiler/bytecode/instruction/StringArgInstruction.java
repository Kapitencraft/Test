package net.kapitencraft.lang.compiler.bytecode.instruction;

import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.compiler.bytecode.ByteCodeBuilder;

public class StringArgInstruction extends CodeInstruction {
    private final String value;

    public StringArgInstruction(Opcode opcode, String value) {
        super(opcode);
        this.value = value;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.injectString(value);
    }

    @Override
    public int length() {
        return 3;
    }
}
