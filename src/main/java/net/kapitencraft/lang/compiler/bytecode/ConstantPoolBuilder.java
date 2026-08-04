package net.kapitencraft.lang.compiler.bytecode;

import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantPoolEntry;

import java.util.ArrayList;
import java.util.List;

public class ConstantPoolBuilder {
    private final List<ConstantPoolEntry.Baked> bakedEntries = new ArrayList<>();

    public int addEntry(ConstantPoolEntry entry) {
        bakedEntries.add(entry.bake(this));
        return bakedEntries.size();
    }

    public int getSize() {
        return bakedEntries.size() + 1;
    }

    public void flush(CacheBuffer buffer) {
        buffer.writeShort(getSize());
        bakedEntries.forEach(entry -> {
            buffer.writeByte(entry.getTag());
            entry.write(buffer);
        });
    }
}
