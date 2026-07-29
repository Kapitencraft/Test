package net.kapitencraft.lang.holder.bytecode;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.List;

public record LineNumberTable(Pair<Integer, Integer>[] lines) {

    public int getLineAt(int ip) {
        int i = 0;
        while (i < lines.length - 1 && lines[i].getFirst() < ip) i++;
        return lines[i].getSecond();
    }

    public void write(CacheBuffer buffer) {
        buffer.writeShort(lines.length);
        for (Pair<Integer, Integer> line : lines()) {
            //pc = left, line = right
            buffer.writeShort(line.getFirst());
            buffer.writeShort(line.getSecond());
        }
    }

    public static LineNumberTable read(BytecodeReader reader) {
        int length = reader.read2b();
        Pair<Integer, Integer>[] lines = new Pair[length];
        for (int i = 0; i < length; i++) {
            lines[i] = new Pair<>(reader.read2b(), reader.read2b());
        }
        return new LineNumberTable(lines);
    }

    public static class Builder {
        private final List<Pair<Integer, Integer>> lineChanges = new ArrayList<>();

        public LineNumberTable build() {
            return new LineNumberTable(lineChanges.toArray(Pair[]::new));
        }

        public void changeIfNecessary(int line, int pc) {
            if (line > -1 && (this.lineChanges.isEmpty() || this.lineChanges.getLast().getSecond() != line)) {
                this.lineChanges.add(Pair.of(pc, line));
            }
        }

        public void clear() {
            this.lineChanges.clear();
        }
    }
}
