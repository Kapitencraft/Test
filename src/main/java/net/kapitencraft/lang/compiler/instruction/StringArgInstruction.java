package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;

public class StringArgInstruction extends SimpleInstruction {
    private final String value;

    public StringArgInstruction(Opcode opcode, String value) {
        super(opcode);
        this.value = value;
    }
}
