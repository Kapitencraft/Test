package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantFloatInfo implements ConstantPoolEntry {
    private float value;

    @Override
    public byte getTag() {
        return 4;
    }

    @Override
    public void write(CacheBuffer buffer) {
        buffer.writeByte(getTag());
        buffer.writeInt(Float.floatToIntBits(value));
    }
}
