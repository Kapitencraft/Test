package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public interface ConstantPoolEntry {

    byte getTag();

    void write(CacheBuffer buffer);
}
