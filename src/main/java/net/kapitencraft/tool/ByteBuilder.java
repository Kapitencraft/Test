package net.kapitencraft.tool;

public class ByteBuilder {
    private byte[] pool;
    private int index;

    public ByteBuilder(int initialSize) {
        pool = new byte[initialSize];
    }

    private void reallocate() {
        byte[] poolN = new byte[pool.length * 2];
        System.arraycopy(pool, 0, poolN, 0, pool.length);
        this.pool = poolN;
    }

    public void write(byte b) {
        if (index >= pool.length) {
            reallocate();
        }
        pool[index++] = b;
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
        if (index + bytes.length >= pool.length) {
            reallocate();
        }
        System.arraycopy(bytes, 0, pool, index, bytes.length);
        index += bytes.length;
    }

    public int index() {
        return index;
    }
}
