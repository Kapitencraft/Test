package net.kapitencraft.lang.compiler.instruction.constant;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.bytecode.storage.Chunk;
import net.kapitencraft.lang.compiler.instruction.SimpleInstruction;

public class IntegerConstantInstruction extends SimpleInstruction {
    private final int value;

    public IntegerConstantInstruction(int value) {
        super(Opcode.I_CONST);
        this.value = value;
    }

    @Override
    public void save(Chunk.Builder builder, int[] instStartIndexes) {
        builder.addIntConstant(value);
    }

    @Override
    public int length() {
        return 3;
    }
}
