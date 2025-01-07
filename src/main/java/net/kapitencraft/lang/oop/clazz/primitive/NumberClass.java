package net.kapitencraft.lang.oop.clazz.primitive;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;

public class NumberClass extends PrimitiveClass {

    public NumberClass() {
        super("num", 0);
    }

    @Override
    public LoxClass checkOperation(OperationType type, Operand operand, ClassReference other) {
        if (other.get().isChildOf(VarTypeManager.NUMBER)) {
            return type.isComparator() ? VarTypeManager.BOOLEAN : other.get();
        }
        return VarTypeManager.VOID;
    }

    @Override
    public Object doOperation(OperationType type, Operand operand, Object self, Object other) {
        if (self instanceof Integer) return VarTypeManager.INTEGER.doOperation(type, operand, self, other);
        if (self instanceof Double)  return VarTypeManager.DOUBLE .doOperation(type, operand, self, other);
        if (self instanceof Float)   return VarTypeManager.FLOAT  .doOperation(type, operand, self, other);
        return null;
    }
}
