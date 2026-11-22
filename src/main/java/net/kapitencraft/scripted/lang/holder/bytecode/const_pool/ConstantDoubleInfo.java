package net.kapitencraft.scripted.lang.holder.bytecode.const_pool;

import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;

public record ConstantDoubleInfo(double value) implements LoadableCPEntry {

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        cpReader.add(new ConstantDoubleInfo(Double.longBitsToDouble(
                (long) reader.read4b() << 32 | reader.read4b()
        )));
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(value);
    }

    @Override
    public Object load() {
        return value;
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