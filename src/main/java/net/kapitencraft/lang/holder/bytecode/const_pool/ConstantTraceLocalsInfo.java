package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.lang.exe.load.BytecodeReader;

public record ConstantTraceLocalsInfo(byte[] data) implements ConstantPoolEntry {

    public static void read(BytecodeReader reader, ConstantPoolReader reader1) {
        int length = reader.read2b();
        reader1.add(new ConstantTraceLocalsInfo(reader.readArray(length)));
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(data);
    }

    public record Baked(byte[] data) implements ConstantPoolEntry.Baked {
        @Override
        public byte getTag() {
            return 2;
        }

        @Override
        public void write(CacheBuffer buffer) {
            buffer.writeShort(data.length);
            buffer.writeArray(data);
        }
    }
}
