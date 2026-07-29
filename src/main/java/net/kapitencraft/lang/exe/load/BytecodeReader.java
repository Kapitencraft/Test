package net.kapitencraft.lang.exe.load;

import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantPoolEntry;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantPoolReader;

public class BytecodeReader {
    private final byte[] data;
    private int index;
    private ConstantPoolReader cpReader;

    public BytecodeReader(byte[] data) {
        this.data = data;
    }

    public void setCpReader(ConstantPoolReader reader) {
        cpReader = reader;
    }

    public <T extends ConstantPoolEntry> T readCpEntry() {
        return cpReader.get(read2b());
    }

    public byte read() {
        return data[index++];
    }

    public int read2b() {
        return ((data[index++] & 255) << 8) | (data[index] & 255);
    }

    public int read4b() {
        return ((((data[index++] & 255) << 8) | (data[index++] & 255) << 8) | (data[index++] & 255) << 8) | (data[index] & 255);
    }

    public byte[] readArray(int l) {
        byte[] data = new byte[l];
        System.arraycopy(this.data, index, data, 0, l);
        index += l;
        return data;
    }

    public void skip(int i) {
        this.index += i;
    }
}
