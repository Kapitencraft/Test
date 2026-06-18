package net.kapitencraft.lang.bytecode.compile;

import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantPoolEntry;

import java.util.ArrayList;
import java.util.List;

public class ConstantPoolBuilder {
    private final List<ConstantPoolEntry> entries;

    public ConstantPoolBuilder() {
        this.entries = new ArrayList<>();
    }

    public int addEntry(ConstantPoolEntry entry) {
        int size = getSize();
        entries.add(entry);
        return size;
    }

    public int getSize() {
        return entries.size() + 1;
    }

    public void flush(CacheBuffer buffer) {
        buffer.writeShort(getSize());
        entries.forEach(entry -> entry.write(buffer));
    }
}
