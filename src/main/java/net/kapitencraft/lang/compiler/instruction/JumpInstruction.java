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
    public void save(Chunk.Builder builder) {
        super.save(builder);
        builder.add2bArg(index);
    }
}
