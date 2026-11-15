package net.kapitencraft.lang.compiler.bytecode.instruction;

import net.kapitencraft.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.holder.bytecode.Chunk;

public class InvokeDynamicInstruction extends CodeInstruction {
    private final String methodSig;
    private final int captureCount;

    public InvokeDynamicInstruction(String methodSig, int captureCount) {
        super(Opcode.INVOKE_DYNAMIC);
        this.methodSig = methodSig;
        this.captureCount = captureCount;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.injectString(methodSig);
        builder.addArg(captureCount);
    }

    @Override
    public int length() {
        return 5;
    }

    @Override
    public String toString() {
        return "InvokeDynamicInstruction{" + super.toString() + ", methodSig = " + methodSig + ", captureCount = " + captureCount + "}";
    }
}
