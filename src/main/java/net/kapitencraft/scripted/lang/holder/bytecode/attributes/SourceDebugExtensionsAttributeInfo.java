package net.kapitencraft.scripted.lang.holder.bytecode.attributes;

import net.kapitencraft.scripted.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;

public class SourceDebugExtensionsAttributeInfo implements AttributeInfo {
    public static AttributeInfo read(BytecodeReader reader) {
        return new SourceDebugExtensionsAttributeInfo();
    }

    @Override
    public String name() {
        return "SourceDebugExtension";
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuffer bytecodeBuilder) {

    }
}
