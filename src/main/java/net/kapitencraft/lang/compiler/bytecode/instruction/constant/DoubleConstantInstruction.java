package net.kapitencraft.lang.compiler.bytecode.instruction.constant;

import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.lang.compiler.bytecode.instruction.CodeInstruction;

public class DoubleConstantInstruction extends CodeInstruction {
    private final double value;

    public DoubleConstantInstruction(double value) {
        super(Opcode.D_CONST);
        this.value = value;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addDoubleConstant(value);
    }

    @Override
    public int length() {
        return 3;
    }
}
