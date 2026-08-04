package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.lang.exe.load.BytecodeReader;

public class ConstantMethodHandleInfo implements ConstantPoolEntry {
    private byte kind;
    private ConstantObjRefInfo obj;

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        ConstantMethodHandleInfo info = new ConstantMethodHandleInfo();
        info.kind = (byte) reader.read();
        info.obj = cpReader.get(reader.read2b());
        cpReader.add(info);
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(kind, builder.addEntry(obj));
    }

    public record Baked(byte kind, int objIndex) implements ConstantPoolEntry.Baked {

        @Override
        public byte getTag() {
            return 15;
        }

        @Override
        public void write(CacheBuffer buffer) {
            buffer.writeByte(kind);
            buffer.writeShort(objIndex);
        }
    }
}
