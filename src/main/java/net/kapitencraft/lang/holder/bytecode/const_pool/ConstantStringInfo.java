package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;

public class ConstantStringInfo implements ConstantPoolEntry {
    private ConstantUtf8Info string;

    public ConstantStringInfo(String value) {
        this.string = ConstantUtf8Info.create(value);
    }

    private ConstantStringInfo() {
    }

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        ConstantStringInfo info = new ConstantStringInfo();
        info.string = cpReader.get(reader.read2b());
        cpReader.add(info);
    }

    @Override
    public byte getTag() {
        return 8;
    }

    @Override
    public void write(CacheBuffer buffer) {
        short s = buffer.writeEntry(string);
        buffer.writeByte(this.getTag());
        buffer.writeShort(s);
    }
}
