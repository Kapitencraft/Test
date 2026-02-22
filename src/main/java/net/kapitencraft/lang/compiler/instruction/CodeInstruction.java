package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.compiler.ByteCodeBuilder;

public class CodeInstruction implements Instruction {
    private final Opcode opcode;

    public CodeInstruction(Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addCode(opcode);
    }

    @Override
    public int length() {
        return 1;
    }
}
