package net.kapitencraft.scripted.lang.holder.bytecode.attributes;

import net.kapitencraft.scripted.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.ConstantUtf8Info;

public class SignatureAttributeInfo implements AttributeInfo {
    private final String signature;

    public SignatureAttributeInfo(String signature) {
        this.signature = signature;
    }

    public static AttributeInfo create(String methodSignature) {
        return new SignatureAttributeInfo(methodSignature);
    }

    public static AttributeInfo read(BytecodeReader reader) {
        ConstantUtf8Info entry = reader.readCpEntry();
        return new SignatureAttributeInfo(entry.value());
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
    public void write(CacheBuffer buffer, BytecodeBuffer bytecodeBuilder) {
        bytecodeBuilder.extractCPEntry(ConstantUtf8Info.create(signature));
    }
}