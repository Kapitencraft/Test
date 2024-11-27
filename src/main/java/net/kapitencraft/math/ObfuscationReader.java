package net.kapitencraft.math;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;

public class ObfuscationReader extends Reader {
    private final Reader in;
    private Integer buffer;

    public ObfuscationReader(Reader in) {
        this.in = in;
    }


    @Override
    public int read(@NotNull char[] cbuf, int off, int len) throws IOException {
        char[] inBuf = new char[len];
        int out = in.read(inBuf, off, len);
        if (out < 1) {
            System.arraycopy(inBuf, 0, cbuf, 0, inBuf.length);
            return out;
        }
        for (int i = 0; i < cbuf.length; i++) {
            if (buffer == null) {
                buffer = (int) inBuf[i];
            } else {
                long v = (long) buffer << 32 | inBuf[i];
                cbuf[i-1] = (char) BitOperationMain.extract(v, true);
                cbuf[i] = (char) BitOperationMain.extract(v, false);
                buffer = null;
            }
        }
        return out;
    }

    @Override
    public void close() throws IOException {
        if (buffer != null) throw new IOException("buffer not empty!");
    }
}
