package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantClassInfo implements ConstantPoolEntry {
    private ConstantUtf8Info target; //Utf8 Info

    @Override
    public byte getTag() {
        return 7;
    }

    @Override
    public void write(CacheBuffer buffer) {
        short t = buffer.writeEntry(target);
        buffer.writeByte(this.getTag());
        buffer.writeShort(t);
    }
}
