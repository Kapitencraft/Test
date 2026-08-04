package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.lang.exe.load.BytecodeReader;

public record ConstantDoubleInfo(double value) implements ConstantPoolEntry {

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        cpReader.add(new ConstantDoubleInfo(Double.longBitsToDouble(
                (long) reader.read4b() << 32 | reader.read4b()
        )));
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(value);
    }

    public record Baked(double value) implements ConstantPoolEntry.Baked {
        @Override
        public byte getTag() {
            return 6;
        }

        @Override
        public void write(CacheBuffer buffer) {
            long l = Double.doubleToLongBits(value);
            buffer.writeInt((int) (l >> 32));
            buffer.writeInt((int) l);
        }
    }
}
