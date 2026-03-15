package net.kapitencraft.lang.compiler.bytecode.optimize;

import net.kapitencraft.lang.exe.Opcode;

public class InstructionPattern {
    private final Opcode target;

    public InstructionPattern(Opcode target) {
        this.target = target;
    }

    public boolean matches(BytecodeOptimizer.Executor executor, int index) {
        return false;
    }
}
