package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantNameAndTypeInfo implements ConstantPoolEntry {
    private ConstantUtf8Info name;
    private ConstantUtf8Info descriptor;

    @Override
    public byte getTag() {
        return 12;
    }

    @Override
    public void write(CacheBuffer buffer) {
        short name = buffer.writeEntry(this.name);
        short desc = buffer.writeEntry(this.descriptor);
        buffer.writeByte(getTag());
        buffer.writeShort(name);
        buffer.writeShort(desc);
    }
}
