package net.kapitencraft.lang.holder.bytecode;

import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.lang.exe.ScriptedCallable;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.lang.holder.bytecode.attributes.AttributeInfo;
import net.kapitencraft.lang.holder.bytecode.attributes.SourceFileAttributeInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.*;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.generated.CompileClass;
import net.kapitencraft.lang.oop.clazz.generated.RuntimeClass;
import net.kapitencraft.lang.oop.field.RuntimeField;
import net.kapitencraft.lang.oop.method.RuntimeCallable;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;

import java.util.*;

public class ClassFile {
    private static final int MAGIC = 0xCAFEBABE;

    private static final int majorVersion = 65;
    private static final int minorVersion = 65535;

    public static byte[] write(CompileClass target) {

        CacheBuffer mainBuffer = new CacheBuffer();

        mainBuffer.writeInt(MAGIC);
        mainBuffer.writeShort(minorVersion);
        mainBuffer.writeShort(majorVersion);

        //constant pool
        ConstantPoolBuilder constantPoolBuilder = new ConstantPoolBuilder();

        CacheBuffer secondPart = new CacheBuffer();

        BytecodeBuffer attributeBuilder = new BytecodeBuffer(secondPart, constantPoolBuilder);

        secondPart.writeShort(0x0001); //access flags
        secondPart.writeShort(constantPoolBuilder.addEntry(ConstantClassInfo.create(VarTypeManager.getClassName(target)))); //this class
        if (target.superclass() != null) {
            secondPart.writeShort(constantPoolBuilder.addEntry(ConstantClassInfo.create(target.superclass()))); //super class
        } else
            secondPart.writeShort(0);

        ClassReference[] interfaces = target.interfaces();
        secondPart.writeShort(interfaces.length); //interface count
        for (ClassReference anInterface : interfaces) {
            secondPart.writeShort(constantPoolBuilder.addEntry(ConstantClassInfo.create(anInterface.absoluteName())));
        }

        FieldInfo[] fieldInfos = target.extractFields();
        attributeBuilder.writeShort(fieldInfos.length);
        for (FieldInfo fieldInfo : fieldInfos) {
            fieldInfo.write(attributeBuilder);
        }
        CacheBuilder builder = new CacheBuilder(constantPoolBuilder);

        MethodInfo[] methodInfos = target.extractMethods(builder);
        attributeBuilder.writeShort(methodInfos.length);
        for (MethodInfo methodInfo : methodInfos) {
            methodInfo.write(attributeBuilder);
        }

        //attributes
        //SourceFile, InnerClasses, EnclosingMethod, SDE, BootstrapMethods,
        //Module, ModulePackages, ModuleMainClass, NestHost, NestMembers,
        //Record, PermittedSubclasses, Signature, Annotations...
        List<AttributeInfo> attributes = new ArrayList<>();
        attributes.add(new SourceFileAttributeInfo(target.name()));
        //attributes.add();

        attributeBuilder.writeShort(attributes.size());
        for (AttributeInfo attribute : attributes) {
            attributeBuilder.cache(attribute);
        }

        constantPoolBuilder.flush(mainBuffer);
        secondPart.transfer(mainBuffer);
        return mainBuffer.toBytes();
    }

    public static RuntimeClass load(byte[] data) {
        BytecodeReader reader = new BytecodeReader(data);
        if (reader.read4b() != MAGIC) {
            throw new IllegalArgumentException("magic number is wrong");
        }
        reader.skip(4); //skip version info
        int cPoolCount = reader.read2b() - 1;
        ConstantPoolReader cpReader = new ConstantPoolReader(cPoolCount);
        reader.setCpReader(cpReader);

        for (int i = 0; i < cPoolCount; i++) {
            int tagId = reader.read();
            switch (tagId) {
                case 1 -> ConstantUtf8Info.read(reader, cpReader);
                case 2 -> ConstantTraceLocalsInfo.read(reader, cpReader);
                case 3 -> ConstantIntegerInfo.read(reader, cpReader);
                case 4 -> ConstantFloatInfo.read(reader, cpReader);
                case 6 -> ConstantDoubleInfo.read(reader, cpReader);
                case 7 -> ConstantClassInfo.read(reader, cpReader);
                case 8 -> ConstantStringInfo.read(reader, cpReader);
                case 9 -> ConstantObjRefInfo.read(reader, cpReader, (byte) 9);
                case 10 -> ConstantObjRefInfo.read(reader, cpReader, (byte) 10);
                case 12 -> ConstantNameAndTypeInfo.read(reader, cpReader);
                case 15 -> ConstantMethodHandleInfo.read(reader, cpReader);
                case 16 -> ConstantMethodTypeInfo.read(reader, cpReader);
                case 18 -> ConstantInvokeDynamicInfo.read(reader, cpReader);
                default -> throw new IllegalArgumentException("no tag with id: " + tagId);
            }
        }
        short modifiers = (short) reader.read2b();
        ConstantClassInfo thisClass = reader.readCpEntry();
        int superClassIdx = reader.read2b();
        ConstantClassInfo superClass;
        if (superClassIdx == 0) {
            superClass = null;
        } else {
            superClass = cpReader.get(superClassIdx);
        }
        int interfaceCount = reader.read2b();
        ConstantClassInfo[] interfaces = new ConstantClassInfo[interfaceCount];
        for (int i = 0; i < interfaceCount; i++) {
            interfaces[i] = reader.readCpEntry();
        }

        int fieldCount = reader.read2b();
        FieldInfo[] fieldInfos = new FieldInfo[fieldCount];
        for (int i = 0; i < fieldCount; i++) {
            fieldInfos[i] = FieldInfo.read(reader);
        }

        int methodCount = reader.read2b();
        MethodInfo[] methodInfos = new MethodInfo[methodCount];
        for (int i = 0; i < methodCount; i++) {
            methodInfos[i] = MethodInfo.read(reader);
        }

        int attributeCount = reader.read2b();
        AttributeInfo[] attributes = new AttributeInfo[attributeCount];
        for (int i = 0; i < attributeCount; i++) {
            attributes[i] = AttributeInfo.readInfo(reader);
        }

        HashMap<String, List<RuntimeCallable>> methods = new HashMap<>();
        for (MethodInfo methodInfo : methodInfos) {
            methods
                    .computeIfAbsent(methodInfo.getName(), s -> new ArrayList<>())
                    .add(methodInfo.toCallable());
        }
        Map<String, DataMethodContainer> bakedMethods = new HashMap<>();
        methods.forEach((s, runtimeCallables) ->
                bakedMethods.put(s, new DataMethodContainer(runtimeCallables.toArray(ScriptedCallable[]::new)))
        );
        Map<String, RuntimeField> fields = new HashMap<>();
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fields.put(fieldInfo.getName(), fieldInfo.toField()) != null) {
                throw new IllegalStateException("duplicate field with name " + fieldInfo.getName());
            }
        }

        String value = thisClass.getValue();

        int pckNameSplit = value.lastIndexOf('/');
        String pck = value.substring(1, pckNameSplit).replaceAll("/", ".");
        String name = value.substring(pckNameSplit + 1, value.length() - 1);
        return new RuntimeClass(
                cpReader.build(),
                bakedMethods,
                fields,
                superClass == null ? null : superClass.getValue(),
                name,
                pck,
                Arrays.stream(interfaces).map(ConstantClassInfo::getValue).toArray(String[]::new),
                modifiers,
                new Annotation[0],
                attributes
        );
    }
}
