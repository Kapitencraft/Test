package net.kapitencraft.lang.bytecode.compile;

import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantPoolEntry;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class CacheBuffer extends OutputStream {

    private final ArrayList<Byte> buffer;

    public CacheBuffer() {
        this.buffer = new ArrayList<>();
    }

    @Override
    public void write(int b) throws IOException {
        this.buffer.add((byte) b);
    }

    public void writeShort(int i) {
        buffer.add((byte) ((i >> 8) & 255));
        buffer.add((byte) (i & 255));
    }

    public void writeInt(int i) {
        buffer.add((byte) ((i >> 24) & 255));
        buffer.add((byte) ((i >> 16) & 255));
        buffer.add((byte) ((i >> 8) & 255));
        buffer.add((byte) (i & 255));
    }

    public void writeByte(byte b) {
        buffer.add(b);
    }

    public short writeEntry(ConstantPoolEntry entry) {
        int loc = buffer.size();
        entry.write(this);
        return (short) loc;
    }

    public void writeArray(byte[] code) {
        for (byte b : code) {
            buffer.add(b);
        }
    }
}
