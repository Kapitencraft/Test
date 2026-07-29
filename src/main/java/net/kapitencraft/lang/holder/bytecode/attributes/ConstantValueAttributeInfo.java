package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantPoolEntry;

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
