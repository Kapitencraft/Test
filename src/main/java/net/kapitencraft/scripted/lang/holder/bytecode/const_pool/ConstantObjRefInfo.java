package net.kapitencraft.scripted.lang.holder.bytecode.const_pool;

import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.tool.StringReader;

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
        return new ConstantObjRefInfo(ConstantClassInfo.create(owner), ConstantNameAndTypeInfo.create(name, description), tag);
    }

    public static ConstantObjRefInfo fromSignature(String signature) {
        StringReader reader = new StringReader(signature);
        ClassReference type = VarTypeManager.parseType(reader);
        String name = reader.readUntil('(');
        return create(type, name, reader.getRemaining(), (byte) 10);
    }

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader, byte tag) {
        ConstantObjRefInfo info = new ConstantObjRefInfo(cpReader.get(reader.read2b()), cpReader.get(reader.read2b()), tag);
        cpReader.add(info);
    }

    @Override
    public ConstantPoolEntry.Baked bake(ConstantPoolBuilder builder) {
        return new Baked(tag, builder.addEntry(clazz), builder.addEntry(nameAndType));
    }

    public String asMethodSignature() {
        return this.clazz.getValue() + this.nameAndType.toSignature();
    }

    public ScriptedCallable getMethod() {
        ScriptedClass scriptedClass = this.clazz.parse();
        return scriptedClass.getMethod(this.nameAndType.descriptor().value());
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