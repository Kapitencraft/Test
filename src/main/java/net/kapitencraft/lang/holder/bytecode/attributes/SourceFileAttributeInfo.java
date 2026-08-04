package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantUtf8Info;

public class SourceFileAttributeInfo implements AttributeInfo {
    private final String value;

    public SourceFileAttributeInfo(String value) {
        this.value = value;
    }

    public static AttributeInfo read(BytecodeReader reader) {
        return new SourceFileAttributeInfo(reader.<ConstantUtf8Info>readCpEntry().value());
    }

    @Override
    public String name() {
        return "SourceFile";
    }

    @Override
    public int length() {
        return 2;
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuffer bytecodeBuilder) {
        bytecodeBuilder.extractCPEntry(new ConstantUtf8Info(value));
    }
}
