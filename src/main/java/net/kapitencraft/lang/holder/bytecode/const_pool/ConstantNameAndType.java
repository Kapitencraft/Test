package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantNameAndType implements ConstantPoolEntry {
    short name;
    short descriptor;

    @Override
    public byte getTag() {
        return 12;
    }

    @Override
    public void write(CacheBuffer buffer) {

    }
}
