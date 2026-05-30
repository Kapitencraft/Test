package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public class ExceptionsAttributeInfo implements AttributeInfo {


    @Override
    public String name() {
        return "Exceptions";
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public void write(CacheBuffer buffer) {

    }
}
