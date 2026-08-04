package net.kapitencraft.lang.compiler.bytecode.instruction;

import net.kapitencraft.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantClassInfo;
import net.kapitencraft.lang.holder.class_ref.ClassReference;

public class WithClassInfoInstruction extends CodeInstruction {
    private final ClassReference owner;

    public WithClassInfoInstruction(Opcode opcode, ClassReference owner) {
        super(opcode);
        this.owner = owner;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.addConstant(ConstantClassInfo.create(owner));
    }

    @Override
    public int length() {
        return 3;
    }
}
