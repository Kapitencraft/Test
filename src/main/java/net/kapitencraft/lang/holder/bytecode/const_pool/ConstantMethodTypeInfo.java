package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantMethodTypeInfo implements ConstantPoolEntry {
    private ConstantUtf8Info descriptor;

    @Override
    public byte getTag() {
        return 16;
    }

    @Override
    public void write(CacheBuffer buffer) {
        short d = buffer.writeEntry(descriptor);
        buffer.writeByte(getTag());
        buffer.writeShort(d);
    }
}
