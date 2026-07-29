package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;

public interface ConstantPoolEntry {

    byte getTag();

    void write(CacheBuffer buffer);
}