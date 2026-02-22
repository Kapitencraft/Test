package net.kapitencraft.lang.compiler.instruction.constant;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.bytecode.storage.Chunk;
import net.kapitencraft.lang.compiler.ByteCodeBuilder;
import net.kapitencraft.lang.compiler.instruction.CodeInstruction;

public class FloatConstantInstruction extends CodeInstruction {
    private final float value;

    public FloatConstantInstruction(float value) {
        super(Opcode.F_CONST);
        this.value = value;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addFloatConstant(value);
    }

    @Override
    public int length() {
        return 3;
    }
}
