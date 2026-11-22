package net.kapitencraft.scripted.lang.holder.bytecode.const_pool;

import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;

public class ConstantStringInfo implements LoadableCPEntry {
    public final ConstantUtf8Info string;

    public static ConstantStringInfo create(String value) {
        return new ConstantStringInfo(ConstantUtf8Info.create(value));
    }

    public ConstantStringInfo(ConstantUtf8Info info) {
        this.string = info;
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(builder.addEntry(string));
    }

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        ConstantStringInfo info = new ConstantStringInfo(cpReader.get(reader.read2b()));
        cpReader.add(info);
    }

    @Override
    public Object load() {
        return string.value();
    }

    public record Baked(int string) implements ConstantPoolEntry.Baked {
        @Override
        public byte getTag() {
            return 8;
        }

        @Override
        public void write(CacheBuffer buffer) {
            buffer.writeShort(string);
        }
    }
}
