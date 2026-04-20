package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantString implements ConstantPoolEntry {
    @Override
    public byte getTag() {
        return 8;
    }

    @Override
    public void write(CacheBuffer buffer) {

    }
}
