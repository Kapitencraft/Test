package net.kapitencraft.lang.holder.bytecode;

import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantUtf8Info;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.List;

public record LocalVariableTable(Entry[] entries) {

    public Pair<String, String> get(int pc, int i) {
        for (Entry entry : entries) {
            if (entry.index == i && entry.startPc <= pc && entry.startPc + entry.length >= pc)
                return Pair.of(entry.name, entry.type);
        }
        return Pair.of("UNKNOWN", "V");
    }

    public void write(CacheBuffer buffer, BytecodeBuffer builder) {
        buffer.writeShort(entries.length);
        for (Entry entry : entries) {
            entry.write(buffer, builder);
        }
    }

    public static LocalVariableTable read(BytecodeReader reader) {
        int length = reader.read2b();
        Entry[] entries = new Entry[length];
        for (int i = 0; i < length; i++) {
            entries[i] = Entry.read(reader);
        }
        return new LocalVariableTable(entries);
    }

    public int bytecodeLength() {
        return 2 + entries.length * 10;
    }

    private record Entry(int startPc, int length, String name, String type, int index) {

        private void write(CacheBuffer buffer, BytecodeBuffer builder) {
            buffer.writeShort(startPc);
            buffer.writeShort(length);
            builder.extractCPEntry(ConstantUtf8Info.create(name));
            builder.extractCPEntry(ConstantUtf8Info.create(type));
            buffer.writeShort(index);
        }

        private static Entry read(BytecodeReader reader) {
            int startPc = reader.read2b(), length = reader.read2b();
            ConstantUtf8Info name = reader.readCpEntry();
            ConstantUtf8Info type = reader.readCpEntry();
            int index = reader.read2b();
            return new Entry(startPc, length, name.value(), type.value(), index);
        }
    }

    public static class Builder {
        private final List<Stub> stubs = new ArrayList<>();
        private final List<Entry> entries = new ArrayList<>();

        public void addLocal(int position, int index, String type, String name) {
            stubs.add(new Stub(position, index, type, name));
        }

        public void endLocal(int id) {

        }

        public LocalVariableTable build(int codePos) {
            this.stubs.forEach(stub -> entries.add(stub.end(codePos))); //end the rest of the lines
            return new LocalVariableTable(this.entries.toArray(Entry[]::new));
        }

        public void clear() {
            this.stubs.clear();
            this.entries.clear();
        }

        private record Stub(int position, int index, String type, String name) {

            public Entry end(int codePos) {
                return new Entry(position, codePos - position, name, type, index);
            }
        }
    }
}
