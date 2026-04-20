package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantClass implements ConstantPoolEntry {
    private short target;

    @Override
    public byte getTag() {
        return 7;
    }

    @Override
    public void write(CacheBuffer buffer) {
    }
}
