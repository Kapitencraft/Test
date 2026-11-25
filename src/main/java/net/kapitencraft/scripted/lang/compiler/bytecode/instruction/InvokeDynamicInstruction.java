package net.kapitencraft.scripted.lang.compiler.bytecode.instruction;

import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.exe.Opcode;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.ConstantInvokeDynamicInfo;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.ConstantMethodHandleInfo;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.ConstantNameAndTypeInfo;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.ConstantObjRefInfo;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;

public class InvokeDynamicInstruction extends CodeInstruction {
    private final String methodSig;
    private final int captureCount;
    private final byte kind; //6 = invoke static, 5 = invoke virtual

    public InvokeDynamicInstruction(String methodSig, int captureCount, int kind) {
        super(Opcode.INVOKE_DYNAMIC);
        this.methodSig = methodSig;
        this.captureCount = captureCount;
        this.kind = (byte) kind;
    }

    @Override
    public void save(Chunk.Builder builder, ByteCodeBuilder.IpContainer ips) {
        super.save(builder, ips);
        ConstantObjRefInfo objRefInfo = ConstantObjRefInfo.fromSignature(methodSig);
        builder.addConstant(new ConstantInvokeDynamicInfo(
                objRefInfo.nameAndType,
                builder.addBootstrapMethod(new ConstantMethodHandleInfo(
                        this.kind,
                        objRefInfo
                )),
                captureCount
        ));
        builder.add2bArg(0); //the JVM wants you to add 2b of 0 for some reason
    }

    @Override
    public int length() {
        return 5;
    }
}
