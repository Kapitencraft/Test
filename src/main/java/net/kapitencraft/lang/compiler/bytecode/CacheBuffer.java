package net.kapitencraft.lang.compiler.bytecode;

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

    public void writeEntry(ConstantPoolEntry.Baked entry) {
        this.writeByte(entry.getTag());
        entry.write(this);
    }

    public void writeArray(byte[] code) {
        for (byte b : code) {
            buffer.add(b);
        }
    }

    public void transfer(CacheBuffer mainBuffer) {
        mainBuffer.buffer.addAll(this.buffer);
    }

    public byte[] toBytes() {
        byte[] data = new byte[this.buffer.size()];
        for (int i = 0; i < this.buffer.size(); i++) {
            data[i] = this.buffer.get(i);
        }
        return data;
    }
}