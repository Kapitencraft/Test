package net.kapitencraft.lang.holder.bytecode;

import net.kapitencraft.lang.bytecode.compile.BytecodeBuilder;
import net.kapitencraft.lang.bytecode.compile.CacheBuffer;
import net.kapitencraft.lang.holder.bytecode.attributes.AttributeInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantUtf8Info;

public class FieldInfo {
    private final short accessFlags;
    private final String name, descriptor; //Constant Utf8 Info
    private final AttributeInfo[] attributes;

    public FieldInfo(short accessFlags, String name, String descriptor, AttributeInfo[] attributes) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.descriptor = descriptor;
        this.attributes = attributes;
    }

    void write(BytecodeBuilder builder) {
        builder.writeShort(accessFlags);
        builder.extractCPEntry(ConstantUtf8Info.create(name));
        builder.extractCPEntry(ConstantUtf8Info.create(descriptor));
        builder.writeShort(attributes.length);
        for (AttributeInfo attribute : attributes) {
            builder.cache(attribute);
        }
    }
}
