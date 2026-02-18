package net.kapitencraft.lang.compiler.instruction.constant;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.compiler.instruction.SimpleInstruction;

public class FloatConstantInstruction extends SimpleInstruction {
    private final float value;

    public FloatConstantInstruction(float value) {
        super(Opcode.F_CONST);
        this.value = value;
    }
}
