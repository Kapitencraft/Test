package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.class_ref.ClassReference;

public class ConstantClassInfo implements ConstantPoolEntry {
    private ConstantUtf8Info target; //Utf8 Info

    public static ConstantClassInfo create(String classLoc) {
        ConstantClassInfo info = new ConstantClassInfo();
        info.target = ConstantUtf8Info.create(classLoc);
        return info;
    }

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        ConstantClassInfo info = new ConstantClassInfo();
        info.target = reader.readCpEntry();
        cpReader.add(info);
    }

    @Override
    public byte getTag() {
        return 7;
    }

    @Override
    public void write(CacheBuffer buffer) {
        short t = buffer.writeEntry(target);
        buffer.writeByte(this.getTag());
        buffer.writeShort(t);
    }

    public ClassReference toReference() {
        return VarTypeManager.directParseType(this.target.value());
    }

    public String getValue() {
        return this.target.value();
    }
}
