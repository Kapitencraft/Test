package net.kapitencraft.lang.holder.bytecode.const_pool;

public class ConstantPoolReader {
    private final ConstantPoolEntry[] entries;
    private int idx;

    public ConstantPoolReader(int size) {
        entries = new ConstantPoolEntry[size];
    }

    public void add(ConstantPoolEntry entry) {
        if (idx == entries.length)
            throw new IllegalStateException("attempting to add more CP entries than expected");
        entries[idx++] = entry;
    }

    public <T extends ConstantPoolEntry> T get(int idx) {
        return (T) entries[idx];
    }

    public ConstantPoolEntry[] build() {
        return entries;
    }
}
