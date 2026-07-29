package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantClassInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantNameAndTypeInfo;

public class EnclosingMethodAttributeInfo implements AttributeInfo {
    private final ConstantClassInfo clazz;
    private final ConstantNameAndTypeInfo info;

    public EnclosingMethodAttributeInfo(ConstantClassInfo clazz, ConstantNameAndTypeInfo info) {
        this.clazz = clazz;
        this.info = info;
    }

    public static AttributeInfo read(BytecodeReader reader) {
        return new EnclosingMethodAttributeInfo(reader.readCpEntry(), reader.readCpEntry());
    }

    @Override
    public String name() {
        return "EnclosingMethod";
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuffer bytecodeBuilder) {
        bytecodeBuilder.extractCPEntry(clazz);
        bytecodeBuilder.extractCPEntry(info);
    }
}
