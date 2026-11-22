package net.kapitencraft.scripted.lang.holder.bytecode.const_pool;

import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;

public class ConstantInvokeDynamicInfo implements ConstantPoolEntry {
    private final ConstantNameAndTypeInfo nameAndTypeInfo;
    private final ConstantMethodHandleInfo methodHandleInfo;
    private final int captureCount;

    public ConstantInvokeDynamicInfo(ConstantNameAndTypeInfo nameAndTypeInfo, ConstantMethodHandleInfo methodHandleInfo, int captureCount) {
        this.nameAndTypeInfo = nameAndTypeInfo;
        this.methodHandleInfo = methodHandleInfo;
        this.captureCount = captureCount;
    }

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        ConstantNameAndTypeInfo nameAndTypeInfo = cpReader.get(reader.read2b());
        ConstantMethodHandleInfo constantMethodHandleInfo = cpReader.get(reader.read2b());
        cpReader.add(new ConstantInvokeDynamicInfo(nameAndTypeInfo, constantMethodHandleInfo, reader.read2b()));
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(builder.addBootstrapMethodEntry(methodHandleInfo), builder.addEntry(nameAndTypeInfo), captureCount);
    }

    public ConstantMethodHandleInfo getHandle() {
        return this.methodHandleInfo;
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
