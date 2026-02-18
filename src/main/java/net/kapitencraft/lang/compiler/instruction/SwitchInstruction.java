package net.kapitencraft.lang.compiler.instruction;

import java.util.List;
import java.util.Objects;

public class SwitchInstruction implements Instruction, JumpableInstruction {
    private final int size;
    private int target;
    private final List<Entry> entries;

    public SwitchInstruction(int size, List<Entry> entries) {
        this.size = size;
        this.entries = entries;
    }

    @Override
    public void setTarget(int target) {
        this.target = target;
    }

    public static final class Entry {
        private final int id;
        private int idx;

        public Entry(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public int idx() {
            return idx;
        }

        public void setIdx(int idx) {
            this.idx = idx;
        }
    }
}
