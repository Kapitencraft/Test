package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.bytecode.storage.Chunk;

public class JumpInstruction extends SimpleInstruction implements JumpableInstruction {
    private int index = -1;

    public JumpInstruction(Opcode opcode) {
        super(opcode);
    }

    public void setTarget(int index) {
        this.index = index;
    }

    @Override
    public void save(Chunk.Builder builder, int[] instStartIndexes) {
        super.save(builder, instStartIndexes);
        builder.add2bArg(instStartIndexes[index]); //index is into instruction array, not bytecode array
    }

    @Override
    public int length() {
        return 3;
    }
}
