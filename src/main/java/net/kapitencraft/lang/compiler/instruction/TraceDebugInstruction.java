package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.bytecode.storage.Chunk;

public class TraceDebugInstruction extends SimpleInstruction {
    private final byte[] locals;

    public TraceDebugInstruction(byte[] locals) {
        super(Opcode.TRACE);
        this.locals = locals;
    }

    @Override
    public void save(Chunk.Builder builder, int[] instStartIndexes) {
        builder.addTraceDebug(locals);
    }

    @Override
    public int length() {
        return 3 + locals.length;
    }
}
