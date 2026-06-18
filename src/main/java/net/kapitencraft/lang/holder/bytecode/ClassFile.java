package net.kapitencraft.lang.holder.bytecode;

import net.kapitencraft.lang.bytecode.compile.BytecodeBuilder;
import net.kapitencraft.lang.bytecode.compile.CacheBuffer;
import net.kapitencraft.lang.bytecode.compile.ConstantPoolBuilder;
import net.kapitencraft.lang.compiler.Synthesizer;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantClassInfo;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.generated.CompileClass;

public class ClassFile {
    private static final int MAGIC = 0xCAFEBABE;

    private static final int majorVersion = 65;
    private static final int minorVersion = 65535;

    public static byte[] write(CompileClass target, Synthesizer synthesizer) {
        CacheBuffer mainBuffer = new CacheBuffer();

        mainBuffer.writeInt(MAGIC);
        mainBuffer.writeShort(minorVersion);
        mainBuffer.writeShort(majorVersion);

        //constant pool
        ConstantPoolBuilder CPbuilder = new ConstantPoolBuilder();
        BytecodeBuilder bytecodeBuilder = new BytecodeBuilder(new CacheBuffer(), CPbuilder);

        ClassReference[] interfaces = target.interfaces();
        bytecodeBuilder.writeShort(interfaces.length); //interface count
        for (ClassReference anInterface : interfaces) {
            bytecodeBuilder.writeShort(CPbuilder.addEntry(ConstantClassInfo.create(anInterface.absoluteName())));
        }
        CacheBuffer secondPart = new CacheBuffer();
        BytecodeBuilder attributeBuilder = new BytecodeBuilder(secondPart, CPbuilder);

        secondPart.writeShort(CPbuilder.addEntry(ConstantClassInfo.create(target.absoluteName()))); //this class
        secondPart.writeShort(CPbuilder.addEntry(ConstantClassInfo.create(target.superclass().absoluteName()))); //super class
        secondPart.writeShort(0x0001); //access flags

        FieldInfo[] fieldInfos = target.extractFields(synthesizer);
        attributeBuilder.writeShort(fieldInfos.length);
        for (FieldInfo fieldInfo : fieldInfos) {
            fieldInfo.write(attributeBuilder);
        }

        MethodInfo[] methodInfos = target.extractMethods(synthesizer);
        attributeBuilder.writeShort(methodInfos.length);
        for (MethodInfo methodInfo : methodInfos) {
            methodInfo.write(attributeBuilder);
        }

        //attributes

        CPbuilder.flush(mainBuffer);
        secondPart.transfer(mainBuffer);
        return mainBuffer.toBytes();
    }
}
