package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

public interface AttributeInfo {

    String name();

    int length();

    void write(CacheBuffer buffer);
}
