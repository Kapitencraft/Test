package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantUtf8 implements ConstantPoolEntry {

    @Override
    public byte getTag() {
        return 1;
    }

    @Override
    public void write(CacheBuffer buffer) {

    }
}
