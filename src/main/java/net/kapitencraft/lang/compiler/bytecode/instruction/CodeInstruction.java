package net.kapitencraft.lang.compiler.bytecode.instruction;

import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.compiler.bytecode.ByteCodeBuilder;

public class CodeInstruction implements Instruction {
    public static final CodeInstruction POP = new CodeInstruction(Opcode.POP);
    public static final CodeInstruction POP_2 = new CodeInstruction(Opcode.POP_2);

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

    public Opcode code() {
        return opcode;
    }
}
