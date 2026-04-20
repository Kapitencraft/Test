package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantInterfaceMethodRef implements ConstantPoolEntry {
    @Override
    public byte getTag() {
        return 11;
    }

    @Override
    public void write(CacheBuffer buffer) {

    }
}
