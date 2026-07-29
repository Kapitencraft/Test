package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;

import java.util.function.BiFunction;

public abstract class ConstantObjRefInfo implements ConstantPoolEntry {
    public final ConstantClassInfo clazz; //Class Info
    public final ConstantNameAndTypeInfo nameAndType; //Name And Type Info

    protected ConstantObjRefInfo(ConstantClassInfo clazz, ConstantNameAndTypeInfo nameAndType) {
        this.clazz = clazz;
        this.nameAndType = nameAndType;
    }

    @Override
    public void write(CacheBuffer buffer) {
        int c = buffer.writeEntry(clazz);
        int nT = buffer.writeEntry(nameAndType);
        buffer.writeByte(this.getTag());
        buffer.writeShort(c);
        buffer.writeShort(nT);
    }

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader, BiFunction<ConstantClassInfo, ConstantNameAndTypeInfo, ConstantObjRefInfo> creator) {
        ConstantObjRefInfo info = creator.apply(cpReader.get(reader.read2b()), cpReader.get(reader.read2b()));
        cpReader.add(info);
    }
}
