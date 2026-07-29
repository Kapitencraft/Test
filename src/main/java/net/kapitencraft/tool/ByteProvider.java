package net.kapitencraft.tool;

import java.io.InputStream;

public class ByteProvider extends InputStream {
    private final byte[] source;
    private int idx = 0;

    public ByteProvider(byte[] source) {
        this.source = source;
    }

    @Override
    public int read() {
        if (source.length >= idx)
            return -1;
        return source[idx++] & 0xFF;
    }
}
