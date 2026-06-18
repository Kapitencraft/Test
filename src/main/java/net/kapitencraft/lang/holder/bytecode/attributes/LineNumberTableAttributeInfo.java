package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.bytecode.compile.BytecodeBuilder;
import net.kapitencraft.lang.bytecode.compile.CacheBuffer;
import net.kapitencraft.lang.bytecode.storage.LineNumberTable;
import net.kapitencraft.tool.Pair;

public class LineNumberTableAttributeInfo implements AttributeInfo {
    LineNumberTable table;

    @Override
    public String name() {
        return "LineNumberTable";
    }

    @Override
    public int length() {
        return table.lines().length * 4;
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuilder bytecodeBuilder) {
        for (Pair<Integer, Integer> line : table.lines()) {
            //pc = left, line = right
            buffer.writeShort(line.left());
            buffer.writeShort(line.right());
        }
    }
}
