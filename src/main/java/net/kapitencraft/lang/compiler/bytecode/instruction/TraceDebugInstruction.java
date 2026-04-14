package net.kapitencraft.lang.compiler.bytecode.instruction;

import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.compiler.bytecode.ByteCodeBuilder;

public class TraceDebugInstruction extends CodeInstruction {
    private final byte[] locals;

    public TraceDebugInstruction(byte[] locals) {
        super(Opcode.TRACE);
        this.locals = locals;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addTraceDebug(locals);
    }

    @Override
    public int length() {
        return 3 + locals.length;
    }
}
