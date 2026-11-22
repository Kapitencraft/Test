package net.kapitencraft.scripted.lang.holder.bytecode.const_pool;

import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.ConstantPoolBuilder;

public interface ConstantPoolEntry {

    Baked bake(ConstantPoolBuilder builder);

    interface Baked {

        byte getTag();

        void write(CacheBuffer buffer);
    }
}
