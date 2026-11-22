package net.kapitencraft.scripted.lang.holder.bytecode;

import net.kapitencraft.scripted.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;
import net.kapitencraft.scripted.lang.holder.bytecode.attributes.AttributeInfo;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.ConstantUtf8Info;
import net.kapitencraft.scripted.lang.oop.field.RuntimeField;

public class FieldInfo {
    private final int accessFlags;
    private final String name, descriptor; //Constant Utf8 Info
    private final AttributeInfo[] attributes;

    public FieldInfo(int accessFlags, String name, String descriptor, AttributeInfo[] attributes) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.descriptor = descriptor;
        this.attributes = attributes;
    }

    void write(BytecodeBuffer builder) {
        builder.writeShort(accessFlags);
        builder.extractCPEntry(ConstantUtf8Info.create(name));
        builder.extractCPEntry(ConstantUtf8Info.create(descriptor));
        builder.writeShort(attributes.length);
        for (AttributeInfo attribute : attributes) {
            builder.cache(attribute);
        }
    }

    public static FieldInfo read(BytecodeReader reader) {
        short modifiers = (short) reader.read2b();
        ConstantUtf8Info name = reader.readCpEntry();
        ConstantUtf8Info descriptor = reader.readCpEntry();
        int attributeCount = reader.read2b();
        AttributeInfo[] attributes = new AttributeInfo[attributeCount];
        for (int i = 0; i < attributeCount; i++) {
            attributes[i] = AttributeInfo.readInfo(reader);
        }
        return new FieldInfo(modifiers, name.value(), descriptor.value(), attributes);
    }

    public RuntimeField toField() {
        return new RuntimeField(
                VarTypeManager.directParseType(this.descriptor),
                this.accessFlags
        );
    }

    public String getName() {
        return name;
    }
}