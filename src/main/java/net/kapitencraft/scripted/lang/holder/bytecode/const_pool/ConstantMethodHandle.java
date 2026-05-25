package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantMethodHandle implements ConstantPoolEntry {
    private byte kind;
    private ConstantObjRefInfo obj;

    @Override
    public byte getTag() {
        return 15;
    }

    @Override
    public void write(CacheBuffer buffer) {
        short c = buffer.writeEntry(obj);
        buffer.writeByte(getTag());
        buffer.writeByte(kind);
        buffer.writeShort(c);
    }
}
