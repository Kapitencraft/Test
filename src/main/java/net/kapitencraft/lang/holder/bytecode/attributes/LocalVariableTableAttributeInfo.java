package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.bytecode.compile.BytecodeBuilder;
import net.kapitencraft.lang.bytecode.compile.CacheBuffer;
import net.kapitencraft.lang.bytecode.storage.LocalVariableTable;

public class LocalVariableTableAttributeInfo implements AttributeInfo {
    LocalVariableTable table;

    @Override
    public String name() {
        return "LocalVariableTable";
    }

    @Override
    public int length() {
        return table.bytecodeLength();
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuilder bytecodeBuilder) {
        table.write(buffer, bytecodeBuilder);
    }
}
