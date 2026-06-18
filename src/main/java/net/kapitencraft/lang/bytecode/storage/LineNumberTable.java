package net.kapitencraft.lang.bytecode.storage;

import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.List;

public record LineNumberTable(Pair<Integer, Integer>[] lines) {

    public int getLineAt(int ip) {
        int i = 0;
        while (i < lines.length - 1 && lines[i].left() < ip) i++;
        return lines[i].right();
    }

    public static class Builder {
        private final List<Pair<Integer, Integer>> lineChanges = new ArrayList<>();

        public void change(int pc, int lineNumber) {
            lineChanges.add(Pair.of(pc, lineNumber));
        }

        public LineNumberTable build() {
            return new LineNumberTable(lineChanges.toArray(Pair[]::new));
        }

        public void changeIfNecessary(int line, int pc) {
            if (line > -1 && (this.lineChanges.isEmpty() || this.lineChanges.getLast().right() != line)) {
                this.lineChanges.add(Pair.of(pc, line));
            }
        }

        public void clear() {
            this.lineChanges.clear();
        }
    }
}
