package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.bytecode.compile.BytecodeBuilder;
import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class SignatureAttributeInfo implements AttributeInfo {
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

    }
}
