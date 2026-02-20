package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.storage.Chunk;

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
    public void save(Chunk.Builder builder, int[] instStartIndexes) {
        builder.addExceptionHandler(
                instStartIndexes[handlerStart],
                instStartIndexes[handlerEnd],
                instStartIndexes[handlerIP],
                builder.injectStringNoArg(className)
        );
    }
}
