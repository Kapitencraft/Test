package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public abstract class ConstantObjRefInfo implements ConstantPoolEntry {
    private ConstantClassInfo classIndex; //Class Info
    private ConstantNameAndTypeInfo nameAndTypeIndex; //Name And Type Info

    @Override
    public void write(CacheBuffer buffer) {
        int c = buffer.writeEntry(classIndex);
        int nT = buffer.writeEntry(nameAndTypeIndex);
        buffer.writeByte(this.getTag());
        buffer.writeShort(c);
        buffer.writeShort(nT);
    }
}
