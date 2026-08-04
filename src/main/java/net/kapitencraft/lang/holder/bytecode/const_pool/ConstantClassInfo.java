package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

/**
 * @param target Utf8 Info
 */
public record ConstantClassInfo(ConstantUtf8Info target) implements ConstantPoolEntry {
    public static ConstantClassInfo create(String classLoc) {
        return new ConstantClassInfo(ConstantUtf8Info.create(classLoc));
    }

    public static ConstantClassInfo create(ClassReference reference) {
        return create(VarTypeManager.getClassName(reference));
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(builder.addEntry(target));
    }

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
        ConstantClassInfo info = new ConstantClassInfo(reader.readCpEntry());
        cpReader.add(info);
    }

    public record Baked(int target) implements ConstantPoolEntry.Baked {
        @Override
        public byte getTag() {
            return 7;
        }

        @Override
        public void write(CacheBuffer buffer) {
            buffer.writeShort(target);
        }
    }

    public ClassReference toReference() {
        return VarTypeManager.directParseType(this.target.value());
    }

    public ScriptedClass parse() {
        return VarTypeManager.directFlatParse(this.target.value());
    }

    public String getValue() {
        return this.target.value();
    }
}
