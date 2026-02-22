package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.storage.Chunk;
import net.kapitencraft.lang.compiler.ByteCodeBuilder;
import net.kapitencraft.lang.holder.class_ref.ClassReference;

public class RegisterLocalInstruction implements Instruction {
    private final int idx;
    private final ClassReference type;
    private final String name;

    public RegisterLocalInstruction(int idx, ClassReference type, String name) {
        this.idx = idx;
        this.type = type;
        this.name = name;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addLocal(idx, type, name);
    }
}
