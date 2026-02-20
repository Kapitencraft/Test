package net.kapitencraft.lang.compiler.instruction.constant;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.bytecode.storage.Chunk;
import net.kapitencraft.lang.compiler.instruction.SimpleInstruction;

public class DoubleConstantInstruction extends SimpleInstruction {
    private final double value;

    public DoubleConstantInstruction(double value) {
        super(Opcode.D_CONST);
        this.value = value;
    }

    @Override
    public void save(Chunk.Builder builder, int[] instStartIndexes) {
        builder.addDoubleConstant(value);
    }

    @Override
    public int length() {
        return 3;
    }
}
