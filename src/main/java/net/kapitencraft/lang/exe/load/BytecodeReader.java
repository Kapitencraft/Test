package net.kapitencraft.lang.exe.load;

import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantPoolEntry;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantPoolReader;

import java.io.InputStream;

public class BytecodeReader extends InputStream {
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

    public int read() {
        return data[index++] & 0xFF;
    }

    public int read2b() {
        return (read() << 8) | read();
    }

    public int read4b() {
        return (read2b() << 16) | read2b();
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
