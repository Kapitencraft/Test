package net.kapitencraft.lang.oop.clazz.primitive;

import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.algebra.OperationType;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

public class IntegerClass extends PrimitiveClass {
    public IntegerClass() {
        super(VarTypeManager.NUMBER, "int", 0);
    }

    @Override
    public ScriptedClass checkOperation(OperationType type, ClassReference other) {
        if (other.get().isChildOf(VarTypeManager.NUMBER)) {
            return type.isComparator() ? VarTypeManager.BOOLEAN : other.get();
        }
        return VarTypeManager.VOID;
    }
}
