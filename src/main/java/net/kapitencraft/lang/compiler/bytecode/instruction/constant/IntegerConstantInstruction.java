package net.kapitencraft.lang.compiler.bytecode.instruction.constant;

import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.lang.compiler.bytecode.instruction.CodeInstruction;

public class IntegerConstantInstruction extends CodeInstruction {
    private final int value;

    public IntegerConstantInstruction(int value) {
        super(Opcode.I_CONST);
        this.value = value;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addIntConstant(value);
    }

    @Override
    public int length() {
        return 3;
    }
}
