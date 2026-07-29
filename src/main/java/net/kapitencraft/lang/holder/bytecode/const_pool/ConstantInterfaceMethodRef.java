package net.kapitencraft.lang.holder.bytecode.const_pool;

public class ConstantInterfaceMethodRef extends ConstantObjRefInfo {
    protected ConstantInterfaceMethodRef(ConstantClassInfo classIndex, ConstantNameAndTypeInfo nameAndTypeIndex) {
        super(classIndex, nameAndTypeIndex);
    }

    @Override
    public byte getTag() {
        return 11;
    }
}
