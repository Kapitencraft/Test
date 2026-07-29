package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.ClassFile;

public class ConstantMethodHandleInfo implements ConstantPoolEntry {
    private byte kind;
    private ConstantObjRefInfo obj;

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        ConstantMethodHandleInfo info = new ConstantMethodHandleInfo();
        info.kind = reader.read();
        info.obj = cpReader.get(reader.read2b());
        cpReader.add(info);
    }

    @Override
    public byte getTag() {
        return 15;
    }

    @Override
    public void write(CacheBuffer buffer) {
        short c = buffer.writeEntry(obj);
        buffer.writeByte(getTag());
        buffer.writeByte(kind);
        buffer.writeShort(c);
    }
}
