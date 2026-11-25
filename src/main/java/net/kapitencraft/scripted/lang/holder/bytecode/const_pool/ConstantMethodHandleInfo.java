package net.kapitencraft.scripted.lang.holder.bytecode.const_pool;

import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.scripted.lang.exe.lambda.ScriptedMethodHandle;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;

public record ConstantMethodHandleInfo(byte kind, ConstantObjRefInfo obj) implements LoadableCPEntry {

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        ConstantMethodHandleInfo info = new ConstantMethodHandleInfo((byte) reader.read(), cpReader.get(reader.read2b()));
        cpReader.add(info);
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(kind, builder.addEntry(obj));
    }

    @Override
    public Object load() {
        return new ScriptedMethodHandle(obj.getMethod(), obj.asMethodSignature(), kind);
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