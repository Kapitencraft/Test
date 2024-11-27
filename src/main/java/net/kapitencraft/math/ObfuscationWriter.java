package net.kapitencraft.math;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

public class ObfuscationWriter extends Writer {
    private final Writer out;
    private Integer buffer;

    public ObfuscationWriter(Writer out) {
        this.out = out;
    }

    @Override
    public void write(char @NotNull [] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            char c = cbuf[i];
            if (buffer != null) {
                long l = BitOperationMain.wive(buffer, c);
                output(l);
            }
            else buffer = (int) c;
        }
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
    }

    @Override
    public void close() throws IOException {
        if (buffer != null) {
            long wived = BitOperationMain.wive(buffer, 0);
            output(wived);
            this.out.close();
        }
    }

    private void output(long l) throws IOException {
        out.write((int) (l >> 32));
        out.write((int) (l & 0xFFFFFFFFL));
    }
}
