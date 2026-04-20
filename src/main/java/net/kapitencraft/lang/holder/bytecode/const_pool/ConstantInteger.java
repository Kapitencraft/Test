package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantInteger implements ConstantPoolEntry {
    private int value;

    @Override
    public byte getTag() {
        return 3;
    }

    @Override
    public void write(CacheBuffer buffer) {

    }
}
