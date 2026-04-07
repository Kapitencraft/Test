package net.kapitencraft.lang.oop.clazz.primitive;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.PrimitiveClass;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.algebra.OperationType;

public class CharacterClass extends PrimitiveClass {
    public CharacterClass() {
        super("char", ' ');
    }

    @Override
    public ScriptedClass checkOperation(OperationType type, ClassReference other) {
        if (other.is(VarTypeManager.INTEGER)) {
            return type.isComparator() ? VarTypeManager.BOOLEAN : VarTypeManager.INTEGER;
        }
        return VarTypeManager.VOID;
    }
}
