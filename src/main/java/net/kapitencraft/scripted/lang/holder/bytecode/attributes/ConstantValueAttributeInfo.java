package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.bytecode.compile.BytecodeBuilder;
import net.kapitencraft.lang.bytecode.compile.CacheBuffer;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantPoolEntry;

public class ConstantValueAttributeInfo implements AttributeInfo {
    private final ConstantPoolEntry constant;

    public ConstantValueAttributeInfo(ConstantPoolEntry constant) {
        this.constant = constant;
    }

    @Override
    public String name() {
        return "ConstantValue";
    }

    @Override
    public int length() {
        return 2;
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuilder bytecodeBuilder) {
        bytecodeBuilder.extractCPEntry(constant);
    }
}
