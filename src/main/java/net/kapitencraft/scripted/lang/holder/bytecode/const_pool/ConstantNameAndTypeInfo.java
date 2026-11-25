package net.kapitencraft.scripted.lang.holder.bytecode.const_pool;

import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;

public record ConstantNameAndTypeInfo(ConstantUtf8Info name, ConstantUtf8Info descriptor) implements ConstantPoolEntry {
    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        ConstantNameAndTypeInfo info = new ConstantNameAndTypeInfo(
                cpReader.get(reader.read2b()),
                cpReader.get(reader.read2b())
        );
        cpReader.add(info);
    }

    public static ConstantNameAndTypeInfo create(String name, String descriptor) {
        return new ConstantNameAndTypeInfo(
                ConstantUtf8Info.create(name),
                ConstantUtf8Info.create(descriptor)
        );
    }

    public String toSignature() {
        return name.value() + descriptor.value();
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
