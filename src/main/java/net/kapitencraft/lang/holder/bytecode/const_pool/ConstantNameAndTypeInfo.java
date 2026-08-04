package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.lang.exe.load.BytecodeReader;

public record ConstantNameAndTypeInfo(ConstantUtf8Info name, ConstantUtf8Info descriptor) implements ConstantPoolEntry {
    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        ConstantNameAndTypeInfo info = new ConstantNameAndTypeInfo(
                cpReader.get(reader.read2b()),
                cpReader.get(reader.read2b())
        );
        cpReader.add(info);
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(builder.addEntry(name), builder.addEntry(descriptor));
    }

    public record Baked(int name, int desc) implements ConstantPoolEntry.Baked {
        @Override
        public byte getTag() {
            return 12;
        }

        @Override
        public void write(CacheBuffer buffer) {
            buffer.writeShort(name);
            buffer.writeShort(desc);
        }
    }
}
