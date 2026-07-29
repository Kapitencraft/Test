package net.kapitencraft.lang.holder.bytecode.attributes;

import net.kapitencraft.lang.compiler.bytecode.BytecodeBuffer;
import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantClassInfo;
import net.kapitencraft.lang.holder.class_ref.ClassReference;

public class ExceptionsAttributeInfo implements AttributeInfo {
    private ConstantClassInfo[] classInfos;

    private ExceptionsAttributeInfo(ConstantClassInfo[] classInfos) {
        this.classInfos = classInfos;
    }

    public static ExceptionsAttributeInfo create(ClassReference[] thrown) {
        ConstantClassInfo[] infos = new ConstantClassInfo[thrown.length];
        for (int i = 0; i < thrown.length; i++) {
            infos[i] = ConstantClassInfo.create(VarTypeManager.getClassName(thrown[i]));
        }
        return new ExceptionsAttributeInfo(infos);
    }

    public static AttributeInfo read(BytecodeReader reader) {
        int l = reader.read2b();
        ConstantClassInfo[] classes = new ConstantClassInfo[l];
        for (int i = 0; i < l; i++) {
            classes[i] = reader.readCpEntry();
        }
        return new ExceptionsAttributeInfo(classes);
    }

    @Override
    public String name() {
        return "Exceptions";
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public void write(CacheBuffer buffer, BytecodeBuffer bytecodeBuilder) {
        bytecodeBuilder.writeShort(this.classInfos.length);
        for (ConstantClassInfo info : classInfos) {
            bytecodeBuilder.extractCPEntry(info);
        }
    }
}
