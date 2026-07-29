package net.kapitencraft.lang.compiler.bytecode;

import net.kapitencraft.lang.holder.bytecode.attributes.AttributeInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantPoolEntry;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantUtf8Info;

public class BytecodeBuffer {
    private final CacheBuffer bytes;
    private final ConstantPoolBuilder constantPoolBuilder;

    public BytecodeBuffer(CacheBuffer bytes, ConstantPoolBuilder constantPoolBuilder) {
        this.bytes = bytes;
        this.constantPoolBuilder = constantPoolBuilder;
    }

    public void extractCPEntry(ConstantPoolEntry entry) {
        bytes.writeShort(constantPoolBuilder.addEntry(entry));
    }

    public void cache(AttributeInfo info) {
        extractCPEntry(ConstantUtf8Info.create(info.name()));
        bytes.writeInt(info.length());
        info.write(bytes, this);
    }

    public void writeShort(int i) {
        bytes.writeShort(i);
    }

    public void writeInt(int i) {
        bytes.writeInt(i);
    }

    public void writeByte(byte b) {
        bytes.writeByte(b);
    }

    public short writeEntry(ConstantPoolEntry entry) {
        return bytes.writeEntry(entry);
    }

    public void writeArray(byte[] code) {
        bytes.writeArray(code);
    }
}
