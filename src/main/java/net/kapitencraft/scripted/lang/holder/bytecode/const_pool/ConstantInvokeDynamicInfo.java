package net.kapitencraft.scripted.lang.holder.bytecode.const_pool;

import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;

public class ConstantInvokeDynamicInfo implements ConstantPoolEntry {
    private final ConstantNameAndTypeInfo nameAndTypeInfo;
    private final int methodHandleInfoIdx;
    private final int captureCount;

    public ConstantInvokeDynamicInfo(ConstantNameAndTypeInfo nameAndTypeInfo, int methodHandleInfo, int captureCount) {
        this.nameAndTypeInfo = nameAndTypeInfo;
        this.methodHandleInfoIdx = methodHandleInfo;
        this.captureCount = captureCount;
    }

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        int bootstrapMethodIdx = reader.read2b();
        ConstantNameAndTypeInfo nameAndTypeInfo = cpReader.get(reader.read2b());
        cpReader.add(new ConstantInvokeDynamicInfo(nameAndTypeInfo, bootstrapMethodIdx, reader.read2b()));
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(methodHandleInfoIdx, builder.addEntry(nameAndTypeInfo), captureCount);
    }

    public int getHandleIdx() {
        return this.methodHandleInfoIdx;
    }

    public ConstantNameAndTypeInfo getNameAndTypeInfo() {
        return nameAndTypeInfo;
    }

    public int getCaptureCount() {
        return captureCount;
    }

    public record Baked(int bootstrapMethodIndex, int nameAndTypeIndex, int captureCount) implements ConstantPoolEntry.Baked {
        @Override
        public byte getTag() {
            return 18;
        }

        @Override
        public void write(CacheBuffer buffer) {
            buffer.writeShort(bootstrapMethodIndex);
            buffer.writeShort(nameAndTypeIndex);
            buffer.writeShort(captureCount);
        }
    }
}
