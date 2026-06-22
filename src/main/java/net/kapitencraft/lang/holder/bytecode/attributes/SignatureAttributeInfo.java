package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.bytecode.compile.BytecodeBuilder;
import net.kapitencraft.lang.bytecode.compile.CacheBuffer;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantUtf8Info;

public class SignatureAttributeInfo implements AttributeInfo {
    private final String signature;

    public SignatureAttributeInfo(String signature) {
        this.signature = signature;
    }

    public static AttributeInfo create(String methodSignature) {
        return new SignatureAttributeInfo(methodSignature);
    }

    @Override
    public String name() {
        return "Signature";
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuilder bytecodeBuilder) {
        bytecodeBuilder.extractCPEntry(ConstantUtf8Info.create(signature));
    }
}
