package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.compiler.bytecode.ConstantPoolBuilder;

public class ConstantModule implements ConstantPoolEntry {

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked();
    }

    public record Baked() implements ConstantPoolEntry.Baked {
        @Override
        public byte getTag() {
            return 19;
        }

        @Override
        public void write(CacheBuffer buffer) {

        }
    }
}
