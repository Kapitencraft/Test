package net.kapitencraft.scripted.lang.holder.bytecode;

import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.bytecode.attributes.AttributeInfo;
import net.kapitencraft.scripted.lang.holder.bytecode.attributes.CodeAttributeInfo;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.ConstantUtf8Info;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.oop.method.RuntimeCallable;
import net.kapitencraft.tool.StringReader;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
    private final int accessFlags;
    private final String name, descriptor;
    private final AttributeInfo[] attributes;

    public MethodInfo(int accessFlags, String name, String descriptor, AttributeInfo[] attributes) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.descriptor = descriptor;
        this.attributes = attributes;
    }

    public static MethodInfo read(BytecodeReader reader) {
        int modifiers = reader.read2b();
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

    public RuntimeCallable toCallable(ClassReference reference) {
        StringReader reader = new StringReader(this.descriptor);
        reader.skip(); //skip open bracket
        List<ClassReference> args = new ArrayList<>();
        while (reader.canRead() && reader.peek() != ')') {
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
                reference,
                attributes
        );
    }

    public String getName() {
        return name;
    }
}
