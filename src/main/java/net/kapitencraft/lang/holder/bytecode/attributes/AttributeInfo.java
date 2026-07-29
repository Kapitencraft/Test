package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantUtf8Info;

public interface AttributeInfo {

    static AttributeInfo readInfo(BytecodeReader reader) {
        ConstantUtf8Info attributeName = reader.readCpEntry();
        int length = reader.read4b();
        return switch (attributeName.value()) {
            case "ConstantValue" -> ConstantValueAttributeInfo.read(reader);
            case "Code" -> CodeAttributeInfo.read(reader);
            case "StackMapTable" -> StackMapTableAttributeInfo.read(reader);
            case "Exceptions" -> ExceptionsAttributeInfo.read(reader);
            case "InnerClasses" -> InnerClassesAttributeInfo.read(reader);
            case "EnclosingMethod" -> EnclosingMethodAttributeInfo.read(reader);
            case "Synthetic" -> SyntheticAttributeInfo.read(reader);
            case "Signature" -> SignatureAttributeInfo.read(reader);
            case "SourceFile" -> SourceFileAttributeInfo.read(reader);
            case "SourceDebugExtension" -> SourceDebugExtensionsAttributeInfo.read(reader);
            case "LineNumberTable" -> LineNumberTableAttributeInfo.read(reader);
            case "LocalVariableTable" -> LocalVariableTableAttributeInfo.read(reader);
            case "LocalVariableTypeTable" -> LocalVariableTypeTableAttributeInfo.read(reader);
            case "Deprecated" -> DeprecatedAttributeInfo.read(reader);
            case "RuntimeVisibleAnnotations" -> RuntimeVisibleAnnotationAttributeInfo.read(reader);
            default -> {
                reader.skip(length);
                yield null;
            }
        };
    }

    String name();

    int length();

    void write(CacheBuffer buffer, BytecodeBuffer bytecodeBuilder);
}
