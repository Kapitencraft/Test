package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;

public class InvokeDynamicInstruction extends CodeInstruction {
    private final String methodSig;
    private final int captureCount;
    private final int kind; //0 = invoke static, 1 = invoke virtual

    public InvokeDynamicInstruction(String methodSig, int captureCount, int kind) {
        super(Opcode.INVOKE_DYNAMIC);
        this.methodSig = methodSig;
        this.captureCount = captureCount;
        this.kind = kind;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        builder.injectString(methodSig);
        builder.addArg(captureCount);
        builder.addArg(kind);
    }

    @Override
    public int length() {
        return 6;
    }

    @Override
    public String toString() {
        return "InvokeDynamicInstruction{" + super.toString() + ", methodSig = " + methodSig + ", captureCount = " + captureCount + "}";
    }
}
