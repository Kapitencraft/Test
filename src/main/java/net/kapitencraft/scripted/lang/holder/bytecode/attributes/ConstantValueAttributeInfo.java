package net.kapitencraft.scripted.lang.holder.bytecode.attributes;

import net.kapitencraft.scripted.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.scripted.lang.compiler.bytecode.ByteCodeBuilder;
import net.kapitencraft.scripted.lang.exe.load.BytecodeReader;
import net.kapitencraft.scripted.lang.holder.bytecode.const_pool.ConstantPoolEntry;

public class ConstantValueAttributeInfo implements AttributeInfo {
    private final ConstantPoolEntry constant;

    public ConstantValueAttributeInfo(ConstantPoolEntry constant) {
        this.constant = constant;
    }

    public static AttributeInfo read(BytecodeReader reader) {
        return new ConstantValueAttributeInfo(reader.readCpEntry());
    }

    @Override
    public String name() {
        return "ConstantValue";
    }

    @Override
    public int length() {
        return 2;
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuffer bytecodeBuilder) {
        bytecodeBuilder.extractCPEntry(constant);
    }
}
