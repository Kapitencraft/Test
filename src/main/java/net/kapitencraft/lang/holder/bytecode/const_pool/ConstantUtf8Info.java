package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.bytecode.compile.CacheBuffer;

import java.io.DataOutputStream;

public class ConstantUtf8Info implements ConstantPoolEntry {
    private String value;

    public static ConstantUtf8Info create(String val) {
        ConstantUtf8Info info = new ConstantUtf8Info();
        info.value = val;
        return info;
    }

    @Override
    public byte getTag() {
        return 1;
    }

    @Override
    public void write(CacheBuffer buffer) {
        buffer.writeByte(this.getTag());
        DataOutputStream stream = new DataOutputStream(buffer);
        try {
            stream.writeUTF(this.value);
        } catch (Exception e) {
            System.err.println("error saving value: " + e.getMessage());
        }
    }
}
