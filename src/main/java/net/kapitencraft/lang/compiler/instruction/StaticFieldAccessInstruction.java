package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;

public class StaticFieldAccessInstruction extends SimpleInstruction {
    private final String className, fieldName;

    public StaticFieldAccessInstruction(Opcode opcode, String className, String fieldName) {
        super(opcode);
        this.className = className;
        this.fieldName = fieldName;
    }
}
