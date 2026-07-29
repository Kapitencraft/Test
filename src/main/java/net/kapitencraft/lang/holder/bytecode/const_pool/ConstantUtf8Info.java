package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.tool.ByteBuilder;
import net.kapitencraft.tool.ByteProvider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record ConstantUtf8Info(String value) implements ConstantPoolEntry {
    public static ConstantUtf8Info create(String val) {
        return new ConstantUtf8Info(val);
    }

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {

        byte[] stringData = reader.readArray(reader.read2b());
        ByteProvider provider = new ByteProvider(stringData);

        DataInputStream stream = new DataInputStream(provider);
        try {
            String s = stream.readUTF();
            cpReader.add(ConstantUtf8Info.create(s));
        } catch (IOException e) {
            System.err.println("error loading string: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte getTag() {
        return 1;
    }

    @Override
    public void write(CacheBuffer buffer) {
        buffer.writeByte(this.getTag());
        ByteBuilder builder = new ByteBuilder(0);
        DataOutputStream stream = new DataOutputStream(builder);
        try {
            stream.writeUTF(this.value);
        } catch (Exception e) {
            System.err.println("error saving value: " + e.getMessage());
        }
        byte[] output = builder.output();
        buffer.writeShort(output.length);
        buffer.writeArray(output);
    }
}
