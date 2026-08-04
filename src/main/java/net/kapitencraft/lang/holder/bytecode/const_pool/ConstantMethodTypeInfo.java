package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.lang.exe.load.BytecodeReader;

public record ConstantMethodTypeInfo(ConstantUtf8Info descriptor) implements ConstantPoolEntry {

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(builder.addEntry(descriptor));
    }

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        ConstantMethodTypeInfo info = new ConstantMethodTypeInfo(reader.readCpEntry());
        cpReader.add(info);
    }

    public record Baked(int descriptorIndex) implements ConstantPoolEntry.Baked {
        @Override
        public byte getTag() {
            return 16;
        }

        @Override
        public void write(CacheBuffer buffer) {
            buffer.writeShort(descriptorIndex);
        }
    }
}
