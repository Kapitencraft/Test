package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.LocalVariableTable;

public class LocalVariableTableAttributeInfo implements AttributeInfo {
    LocalVariableTable table;

    public LocalVariableTableAttributeInfo(LocalVariableTable table) {
        this.table = table;
    }

    public static AttributeInfo read(BytecodeReader reader) {
        return new LocalVariableTableAttributeInfo(LocalVariableTable.read(reader));
    }

    @Override
    public String name() {
        return "LocalVariableTable";
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
