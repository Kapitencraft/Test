package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.LineNumberTable;

public class LineNumberTableAttributeInfo implements AttributeInfo {
    LineNumberTable table;

    public LineNumberTableAttributeInfo(LineNumberTable table) {
        this.table = table;
    }

    public static AttributeInfo read(BytecodeReader reader) {
        return new LineNumberTableAttributeInfo(LineNumberTable.read(reader));
    }

    @Override
    public String name() {
        return "LineNumberTable";
    }

    @Override
    public int length() {
        return table.lines().length * 4;
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuffer bytecodeBuilder) {
        table.write(buffer);
    }
}
