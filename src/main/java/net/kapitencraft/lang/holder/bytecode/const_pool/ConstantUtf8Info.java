package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.lang.exe.load.BytecodeReader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record ConstantUtf8Info(String value) implements ConstantPoolEntry {
    public static ConstantUtf8Info create(String val) {
        return new ConstantUtf8Info(val);
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(value);
    }

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {

        DataInputStream stream = new DataInputStream(reader);
        try {
            String s = stream.readUTF();
            cpReader.add(ConstantUtf8Info.create(s));
        } catch (IOException e) {
            System.err.println("error loading string: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public record Baked(String value) implements ConstantPoolEntry.Baked {
        @Override
        public byte getTag() {
            return 1;
        }

        @Override
        public void write(CacheBuffer buffer) {
            DataOutputStream stream = new DataOutputStream(buffer);
            try {
                stream.writeUTF(this.value);
            } catch (Exception e) {
                System.err.println("error saving value: " + e.getMessage());
            }
        }
    }
}
