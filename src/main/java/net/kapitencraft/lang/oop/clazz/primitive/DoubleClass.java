package net.kapitencraft.lang.oop.clazz.primitive;

import net.kapitencraft.lang.exe.VirtualMachine;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.algebra.Operand;
import net.kapitencraft.lang.exe.algebra.OperationType;

public class DoubleClass extends PrimitiveClass {

    public DoubleClass() {
        super(VarTypeManager.NUMBER, "double", 0d);
    }

    @Override
    public ScriptedClass checkOperation(OperationType type, ClassReference other) {
        if (other.get().isChildOf(VarTypeManager.NUMBER)) {
            return type.isComparator() ? VarTypeManager.BOOLEAN : VarTypeManager.DOUBLE;
        }
        return VarTypeManager.VOID;
    }
}
