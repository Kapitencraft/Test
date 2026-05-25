package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ConstantValueAttributeInfo implements AttributeInfo {
    private short attributeName; //Constant Utf8
    private short constant; //any value constant

    @Override
    public String name() {
        return "ConstantValue";
    }

    @Override
    public int length() {
        return 2;
    }

    @Override
    public void write(CacheBuffer buffer) {
        buffer.writeShort(attributeName);
        buffer.writeInt(2);
        buffer.writeShort(constant);
    }
}
