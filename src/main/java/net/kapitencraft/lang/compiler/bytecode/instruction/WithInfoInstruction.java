package net.kapitencraft.lang.compiler.bytecode.instruction;

import net.kapitencraft.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantObjRefInfo;
import net.kapitencraft.lang.holder.class_ref.ClassReference;

public class WithInfoInstruction extends CodeInstruction {
    private final ClassReference owner;
    private final String name, desc;
    private final byte tag;

    public WithInfoInstruction(Opcode opcode, ClassReference owner, String name, String desc, byte tag) {
        super(opcode);
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.tag = tag;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.addConstant(ConstantObjRefInfo.create(owner, name, desc, tag));
    }

    @Override
    public int length() {
        return 3;
    }
}
