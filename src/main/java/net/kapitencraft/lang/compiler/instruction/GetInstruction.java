package net.kapitencraft.lang.compiler.instruction;

import net.kapitencraft.lang.bytecode.exe.Opcode;

public class GetInstruction extends SimpleInstruction {
    private final int id;

    public GetInstruction(int id) {
        super(Opcode.GET);
        this.id = id;
    }
}
