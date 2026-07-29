package net.kapitencraft.lang.holder.bytecode.const_pool;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.load.BytecodeReader;

public class ConstantInvokeDynamicInfo implements ConstantPoolEntry {

    public static void read(BytecodeReader reader, ConstantPoolReader cpReader) {
    }

    @Override
    public byte getTag() {
        return 18;
    }

    @Override
    public void write(CacheBuffer buffer) {

    }
}
