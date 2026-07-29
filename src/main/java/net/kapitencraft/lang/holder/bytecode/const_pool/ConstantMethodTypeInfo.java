package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;

public class ConstantMethodTypeInfo implements ConstantPoolEntry {
    private ConstantUtf8Info descriptor;

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        ConstantMethodTypeInfo info = new ConstantMethodTypeInfo();
        info.descriptor = cpReader.get(reader.read2b());
        cpReader.add(info);
    }

    @Override
    public byte getTag() {
        return 16;
    }

    @Override
    public void write(CacheBuffer buffer) {
        short d = buffer.writeEntry(descriptor);
        buffer.writeByte(getTag());
        buffer.writeShort(d);
    }
}
