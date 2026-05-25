package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantDoubleInfo implements ConstantPoolEntry {
    private double value;

    @Override
    public byte getTag() {
        return 6;
    }

    @Override
    public void write(CacheBuffer buffer) {
        buffer.writeByte(getTag());
        long l = Double.doubleToLongBits(value);
        buffer.writeInt((int) (l >> 32));
        buffer.writeInt((int) l);
    }
}
