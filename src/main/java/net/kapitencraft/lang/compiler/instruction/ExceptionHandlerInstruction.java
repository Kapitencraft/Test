package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.storage.Chunk;
import net.kapitencraft.lang.compiler.ByteCodeBuilder;

public class ExceptionHandlerInstruction implements Instruction {
    private final int handlerStart;
    private final int handlerEnd;
    private final int handlerIP;
    private final String className;

    public ExceptionHandlerInstruction(int handlerStart, int handlerEnd, int handlerIP, String className) {
        this.handlerStart = handlerStart;
        this.handlerEnd = handlerEnd;
        this.handlerIP = handlerIP;
        this.className = className;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        builder.addExceptionHandler(
                ips.getIp(handlerStart),
                ips.getIp(handlerEnd),
                ips.getIp(handlerIP),
                builder.injectStringNoArg(className)
        );
    }
}
