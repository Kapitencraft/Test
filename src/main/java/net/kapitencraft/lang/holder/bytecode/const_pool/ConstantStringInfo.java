package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantStringInfo implements ConstantPoolEntry {
    private ConstantUtf8Info string;

    @Override
    public byte getTag() {
        return 8;
    }

    @Override
    public void write(CacheBuffer buffer) {
        short s = buffer.writeEntry(string);
        buffer.writeByte(this.getTag());
        buffer.writeShort(s);
    }
}
