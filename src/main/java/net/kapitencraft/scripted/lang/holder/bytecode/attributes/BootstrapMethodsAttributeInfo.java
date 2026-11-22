package net.kapitencraft.scripted.lang.holder.bytecode.attributes;

import net.kapitencraft.scripted.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.ConstantMethodHandleInfo;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.LoadableCPEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BootstrapMethodsAttributeInfo implements AttributeInfo {
    private final Entry[] entries;

    private BootstrapMethodsAttributeInfo(Entry[] entries) {
        this.entries = entries;
    }

    @Override
    public String name() {
        return "BootstrapMethods";
    }

    @Override
    public int length() {
        return Arrays.stream(this.entries).mapToInt(e -> 4 + e.loadableCPs.length * 2).sum();
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuffer bytecodeBuilder) {
        bytecodeBuilder.writeShort(entries.length);
        for (Entry entry : entries) {
            bytecodeBuilder.extractCPEntry(entry.methodHandleInfo);
            bytecodeBuilder.writeShort(entry.loadableCPs.length);
            for (LoadableCPEntry loadableCP : entry.loadableCPs) {
                bytecodeBuilder.extractCPEntry(loadableCP);
            }
        }
    }

    private record Entry(ConstantMethodHandleInfo methodHandleInfo, LoadableCPEntry[] loadableCPs) {

    }

    public static class Builder {
        private final List<Entry> entries = new ArrayList<>();

        public int add(ConstantMethodHandleInfo methodHandleInfo, LoadableCPEntry[] loadableCPs) {
            int size = entries.size();
            entries.add(new Entry(methodHandleInfo, loadableCPs));
            return size;
        }

        public BootstrapMethodsAttributeInfo build() {
            return new BootstrapMethodsAttributeInfo(entries.toArray(Entry[]::new));
        }

        public void clear() {
            this.entries.clear();
        }
    }
}
