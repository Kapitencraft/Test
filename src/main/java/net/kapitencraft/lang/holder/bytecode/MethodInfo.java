package net.kapitencraft.lang.holder.bytecode;

import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.lang.holder.bytecode.attributes.AttributeInfo;
import net.kapitencraft.lang.holder.bytecode.attributes.CodeAttributeInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantUtf8Info;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.method.RuntimeCallable;
import net.kapitencraft.tool.StringReader;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
    private final short accessFlags;
    private final String name, descriptor;
    private final AttributeInfo[] attributes;

    public MethodInfo(short accessFlags, String name, String descriptor, AttributeInfo[] attributes) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.descriptor = descriptor;
        this.attributes = attributes;
    }

    public static MethodInfo read(BytecodeReader reader) {
        short modifiers = (short) reader.read2b();
        ConstantUtf8Info name = reader.readCpEntry();
        ConstantUtf8Info descriptor = reader.readCpEntry();
        int attributesLength = reader.read2b();
        AttributeInfo[] attributes = new AttributeInfo[attributesLength];
        for (int i = 0; i < attributesLength; i++) {
            attributes[i] = AttributeInfo.readInfo(reader);
        }
        return new MethodInfo(modifiers, name.value(), descriptor.value(), attributes);
    }

    void write(BytecodeBuffer bytecodeBuilder) {
        bytecodeBuilder.writeShort(accessFlags);
        bytecodeBuilder.extractCPEntry(ConstantUtf8Info.create(name));
        bytecodeBuilder.extractCPEntry(ConstantUtf8Info.create(descriptor));
        bytecodeBuilder.writeShort(attributes.length);
        for (AttributeInfo attribute : attributes) {
            bytecodeBuilder.cache(attribute);
        }
    }

    public RuntimeCallable toCallable() {
        StringReader reader = new StringReader(this.descriptor);
        reader.skip();
        List<ClassReference> args = new ArrayList<>();
        while (reader.peek() != ')') {
            args.add(VarTypeManager.parseType(reader));
        }
        Chunk c = null;
        if (!Modifiers.isAbstract(this.accessFlags)) {
            for (AttributeInfo attribute : this.attributes) {
                if (attribute.name().equals("Code")) {
                    CodeAttributeInfo codeAttributeInfo = (CodeAttributeInfo) attribute;
                    c = codeAttributeInfo.chunk;
                }
            }
        }
        reader.skip(); //skip closing bracket
        ClassReference retType = VarTypeManager.parseType(reader);
        return new RuntimeCallable(
                retType, args, List.of(),
                c,
                accessFlags,
                new Annotation[0],
                attributes
        );
    }

    public String getName() {
        return name;
    }
}
