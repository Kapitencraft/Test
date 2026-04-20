package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantPackage implements ConstantPoolEntry {
    @Override
    public byte getTag() {
        return 20;
    }

    @Override
    public void write(CacheBuffer buffer) {

    }
}
