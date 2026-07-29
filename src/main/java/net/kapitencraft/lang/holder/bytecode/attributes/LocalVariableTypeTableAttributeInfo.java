package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.holder.bytecode.LocalVariableTable;

public class LocalVariableTypeTableAttributeInfo implements AttributeInfo {
    LocalVariableTable table;

    @Override
    public String name() {
        return "LocalVariableTypeTable";
    }

    @Override
    public int length() {
        return table.bytecodeLength();
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuffer bytecodeBuilder) {
        table.write(buffer, bytecodeBuilder);
    }
}
