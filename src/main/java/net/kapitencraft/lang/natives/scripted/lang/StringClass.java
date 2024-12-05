package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.oop.clazz.NativeClass;
import net.kapitencraft.lang.oop.method.builder.DataMethodContainer;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;

import java.util.Map;

public class StringClass extends NativeClass {
    private static final Map<String, DataMethodContainer> METHODS = Map.of(

    );

    public StringClass() {
        super("String", "scripted.lang", Map.of(), Map.of(), METHODS, DataMethodContainer.of(), VarTypeManager.OBJECT.get(), false, false, false);
    }

    @Override
    public LoxClass checkOperation(OperationType type, Operand operand, LoxClass other) {
        return type == OperationType.ADDITION ? VarTypeManager.STRING.get() : super.checkOperation(type, operand, other);
    }

    @Override
    public Object doOperation(OperationType type, Operand operand, Object self, Object other) {
        String otherStr = Interpreter.stringify(other); //TODO ensure extracting ClassInstance
        return operand == Operand.LEFT ? self + otherStr : otherStr + self;
    }
}
