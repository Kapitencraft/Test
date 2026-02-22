package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.compiler.ByteCodeBuilder;

public class LocalInstruction extends CodeInstruction {
    private final int id;

    public LocalInstruction(Opcode opcode, int id) {
        super(opcode);
        this.id = id;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.addArg(id);
    }

    @Override
    public int length() {
        return 2;
    }
}
