package net.kapitencraft.lang.oop.clazz.primitive;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.algebra.Operand;
import net.kapitencraft.lang.exe.algebra.OperationType;

public class NumberClass extends PrimitiveClass {

    public NumberClass() {
        super("num", 0);
    }

    @Override
    public ScriptedClass checkOperation(OperationType type, ClassReference other) {
        if (other.get().isChildOf(VarTypeManager.NUMBER)) {
            return type.isComparator() ? VarTypeManager.BOOLEAN : other.get();
        }
        return VarTypeManager.VOID;
    }
}
