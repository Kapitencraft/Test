package net.kapitencraft.scripted.lang.compiler.bytecode;

import net.kapitencraft.scripted.lang.holder.bytecode.attributes.BootstrapMethodsAttributeInfo;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.ConstantMethodHandleInfo;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.ConstantPoolEntry;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.LoadableCPEntry;

import java.util.ArrayList;
import java.util.List;

public class ConstantPoolBuilder {
    private final List<ConstantPoolEntry.Baked> bakedEntries = new ArrayList<>();
    private final BootstrapMethodsAttributeInfo.Builder bootstrapMethodsBuilder = new BootstrapMethodsAttributeInfo.Builder();

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

    public int addBootstrapMethodEntry(ConstantMethodHandleInfo methodHandleInfo) {
        return bootstrapMethodsBuilder.add(methodHandleInfo, new LoadableCPEntry[0]);
    }

    public BootstrapMethodsAttributeInfo buildBootstrapMethods() {
        return bootstrapMethodsBuilder.build();
    }
}