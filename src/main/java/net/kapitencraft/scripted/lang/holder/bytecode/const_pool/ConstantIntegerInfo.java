package net.kapitencraft.scripted.lang.holder.bytecode.const_pool;

import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;

public record ConstantIntegerInfo(int value) implements LoadableCPEntry {

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        cpReader.add(new ConstantIntegerInfo(reader.read4b()));
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(value);
    }

    @Override
    public Object load() {
        return value;
    }

    public record Baked(int value) implements ConstantPoolEntry.Baked {
        @Override
        public byte getTag() {
            return 3;
        }

        @Override
        public void write(CacheBuffer buffer) {
            buffer.writeInt(value);
        }
    }
}
