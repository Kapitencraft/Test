package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;

public class LocalInstruction extends SimpleInstruction {
    private final int id;

    public LocalInstruction(Opcode opcode, int id) {
        super(opcode);
        this.id = id;
    }
}
