package net.kapitencraft.lang.holder;

import net.kapitencraft.lang.holder.bytecode.const_pool.*;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.exe.VarTypeManager;

public record LiteralHolder(Object value, ScriptedClass type) {

    public static final LiteralHolder EMPTY = new LiteralHolder(null, null);

    public ConstantPoolEntry toBytecode() {
        if (this == EMPTY)
            throw new IllegalAccessError("can not convert empty literal to bytecode");
        if (type == VarTypeManager.INTEGER) {
            return new ConstantIntegerInfo((Integer) value);
        } else if (type == VarTypeManager.FLOAT) {
            return new ConstantFloatInfo((Float) value);
        } else if (type == VarTypeManager.DOUBLE) {
            return new ConstantDoubleInfo((Double) value);
        } else if (type == VarTypeManager.BOOLEAN) {
            return new ConstantIntegerInfo((Boolean) value ? 1 : 0);
        } else if (type == VarTypeManager.STRING.get()) {
            return ConstantStringInfo.create((String) value);
        }
        throw new IllegalArgumentException("unknown literal holder type: " + type);
    }
}
