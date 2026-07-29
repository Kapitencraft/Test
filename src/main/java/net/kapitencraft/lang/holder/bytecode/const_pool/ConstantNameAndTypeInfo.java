package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
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
    public byte getTag() {
        return 12;
    }

    @Override
    public void write(CacheBuffer buffer) {
        short name = buffer.writeEntry(this.name);
        short desc = buffer.writeEntry(this.descriptor);
        buffer.writeByte(getTag());
        buffer.writeShort(name);
        buffer.writeShort(desc);
    }
}
