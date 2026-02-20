package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.bytecode.storage.Chunk;

import java.util.List;
import java.util.Objects;


public class SwitchInstruction extends SimpleInstruction implements JumpableInstruction {
    private final int size;
    private int target;
    private final List<Entry> entries;

    public SwitchInstruction(int size, List<Entry> entries) {
        super(Opcode.SWITCH);
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

    @Override
    public void save(Chunk.Builder builder, int[] instStartIndexes) {
        super.save(builder, instStartIndexes);
        builder.add2bArg(instStartIndexes[target]);
        builder.add2bArg(size);
        for (Entry entry : entries) {
            builder.add4bArg(entry.id);
            builder.add2bArg(instStartIndexes[entry.idx]);
        }
    }

    @Override
    public int length() {
        return 5 + entries.size() * 6;
    }
}
