package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.lang.exe.load.BytecodeReader;

public record ConstantFloatInfo(float value) implements ConstantPoolEntry {

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        cpReader.add(new ConstantFloatInfo(Float.intBitsToFloat(reader.read4b())));
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(value);
    }

    public record Baked(float value) implements ConstantPoolEntry.Baked {
        @Override
        public byte getTag() {
            return 4;
        }

        @Override
        public void write(CacheBuffer buffer) {
            buffer.writeInt(Float.floatToIntBits(value));
        }
    }
}
