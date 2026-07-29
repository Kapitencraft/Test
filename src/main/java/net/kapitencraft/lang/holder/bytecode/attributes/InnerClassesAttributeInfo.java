package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;

public class InnerClassesAttributeInfo implements AttributeInfo {
    @Override
    public String name() {
        return "InnerClasses";
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuffer bytecodeBuilder) {

    }
}
