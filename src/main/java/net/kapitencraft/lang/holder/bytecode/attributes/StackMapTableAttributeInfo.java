package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

import java.util.Stack;

//TODO this is pain
public class StackMapTableAttributeInfo implements AttributeInfo {
    @Override
    public String name() {
        return "StackMapTable";
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public void write(CacheBuffer buffer) {

    }
}
