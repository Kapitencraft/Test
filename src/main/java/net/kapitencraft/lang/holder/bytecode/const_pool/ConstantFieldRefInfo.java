package net.kapitencraft.lang.holder.bytecode.const_pool;

public class ConstantFieldRefInfo extends ConstantObjRefInfo {

    public ConstantFieldRefInfo(ConstantClassInfo classIndex, ConstantNameAndTypeInfo nameAndTypeIndex) {
        super(classIndex, nameAndTypeIndex);
    }

    @Override
    public byte getTag() {
        return 9;
    }
}
