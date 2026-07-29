package net.kapitencraft.lang.holder.bytecode.const_pool;

public class ConstantMethodRefInfo extends ConstantObjRefInfo {
    public ConstantMethodRefInfo(ConstantClassInfo classIndex, ConstantNameAndTypeInfo nameAndTypeIndex) {
        super(classIndex, nameAndTypeIndex);
    }

    @Override
    public byte getTag() {
        return 10;
    }
}
