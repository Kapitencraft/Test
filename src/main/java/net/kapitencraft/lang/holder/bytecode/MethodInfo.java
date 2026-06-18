package net.kapitencraft.lang.holder.bytecode;

import net.kapitencraft.lang.bytecode.compile.BytecodeBuilder;
import net.kapitencraft.lang.holder.bytecode.attributes.AttributeInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantUtf8Info;

public class MethodInfo {
    private final short accessFlags;
    private final String name, descriptor;
    private final AttributeInfo[] attributes;

    public MethodInfo(short accessFlags, String name, String descriptor, AttributeInfo[] attributes) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.descriptor = descriptor;
        this.attributes = attributes;
    }

    void write(BytecodeBuilder bytecodeBuilder) {
        bytecodeBuilder.writeShort(accessFlags);
        bytecodeBuilder.extractCPEntry(ConstantUtf8Info.create(name));
        bytecodeBuilder.extractCPEntry(ConstantUtf8Info.create(descriptor));
        bytecodeBuilder.writeShort(attributes.length);
        for (AttributeInfo attribute : attributes) {
            bytecodeBuilder.cache(attribute);
        }
    }
}
