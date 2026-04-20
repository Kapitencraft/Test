package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantFloat implements ConstantPoolEntry {
    @Override
    public byte getTag() {
        return 4;
    }

    @Override
    public void write(CacheBuffer buffer) {

    }
}
