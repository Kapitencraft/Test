package net.kapitencraft.lang.oop.clazz.primitive;

import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;

public class CharacterClass extends PrimitiveClass {
    public CharacterClass() {
        super("char", ' ');
    }

    @Override
    public LoxClass checkOperation(OperationType type, Operand operand, LoxClass other) {
        return other.isChildOf(VarTypeManager.INTEGER) ? VarTypeManager.INTEGER : VarTypeManager.VOID;
    }

    @Override
    public Object doOperation(OperationType type, Operand operand, Object self, Object other) {
        return VarTypeManager.INTEGER.doOperation(type, operand, self, other);
    }
}
