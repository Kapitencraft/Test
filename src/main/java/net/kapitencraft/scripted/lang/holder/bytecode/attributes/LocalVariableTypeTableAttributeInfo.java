package net.kapitencraft.scripted.lang.holder.bytecode.attributes;

import net.kapitencraft.scripted.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;
import net.kapitencraft.scripted.lang.holder.bytecode.LocalVariableTable;

public class LocalVariableTypeTableAttributeInfo implements AttributeInfo {
    LocalVariableTable table;

    public static AttributeInfo read(BytecodeReader reader) {
        return new LocalVariableTypeTableAttributeInfo();
    }

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