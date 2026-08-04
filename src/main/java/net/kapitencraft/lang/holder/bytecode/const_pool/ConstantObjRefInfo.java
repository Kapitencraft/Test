package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.class_ref.ClassReference;

public class ConstantObjRefInfo implements ConstantPoolEntry {
    public final ConstantClassInfo clazz; //Class Info
    public final ConstantNameAndTypeInfo nameAndType; //Name And Type Info
    private final byte tag;

    protected ConstantObjRefInfo(ConstantClassInfo clazz, ConstantNameAndTypeInfo nameAndType, byte tag) {
        this.clazz = clazz;
        this.nameAndType = nameAndType;
        this.tag = tag;
    }

    public static ConstantObjRefInfo create(ClassReference owner, String name, String description, byte tag) {
        return new ConstantObjRefInfo(ConstantClassInfo.create(owner), new ConstantNameAndTypeInfo(ConstantUtf8Info.create(name), ConstantUtf8Info.create(description)), tag);
    }

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader, byte tag) {
        ConstantObjRefInfo info = new ConstantObjRefInfo(cpReader.get(reader.read2b()), cpReader.get(reader.read2b()), tag);
        cpReader.add(info);
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(tag, builder.addEntry(clazz), builder.addEntry(nameAndType));
    }

    public record Baked(byte tag, int classIndex, int nameIndex) implements ConstantPoolEntry.Baked {
        @Override
        public byte getTag() {
            return tag;
        }

        @Override
        public void write(CacheBuffer buffer) {
            buffer.writeShort(classIndex);
            buffer.writeShort(nameIndex);
        }
    }
}
