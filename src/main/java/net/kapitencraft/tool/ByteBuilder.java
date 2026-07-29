package net.kapitencraft.tool;

import java.io.OutputStream;

public class ByteBuilder extends OutputStream {
    private byte[] pool;
    private int index;

    public ByteBuilder(int initialSize) {
        pool = new byte[Math.max(initialSize, 8)];
    }

    private void reallocate() {
        byte[] poolN = new byte[pool.length * 2];
        System.arraycopy(pool, 0, poolN, 0, pool.length);
        this.pool = poolN;
    }

    public void write(int b) {
        if (index >= pool.length) {
            reallocate();
        }
        pool[index++] = (byte) (b & 255);
    }

    public void write16BitShort(short b) {
        this.write((byte) (b & 255));
        this.write((byte) ((b >> 8) & 255));
    }

    public void write32BitInt(int constant) {
        for (int i = 0; i < 4; i++) {
            this.write((byte) ((constant >> (8 * i)) & 255));
        }
    }

    public void writeArray(byte[] bytes) {
        this.write((byte) bytes.length);
        while (index + bytes.length >= pool.length) {
            reallocate();
        }
        System.arraycopy(bytes, 0, pool, index, bytes.length);
        index += bytes.length;
    }

    public int index() {
        return index;
    }

    public byte[] output() {
        byte[] bytes = new byte[index];
        System.arraycopy(pool, 0, bytes, 0, index);
        return bytes;
    }
}
