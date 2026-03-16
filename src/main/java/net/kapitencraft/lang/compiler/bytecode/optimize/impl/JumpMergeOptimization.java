package net.kapitencraft.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.lang.compiler.bytecode.instruction.JumpInstruction;
import net.kapitencraft.lang.compiler.bytecode.optimize.BytecodeOptimizer;
import net.kapitencraft.lang.compiler.bytecode.optimize.SimpleOptimization;
import net.kapitencraft.lang.exe.Opcode;

public class JumpMergeOptimization implements SimpleOptimization {
    @Override
    public void tryExecute(BytecodeOptimizer.Executor executor, int index) {
        if (executor.getInstruction(index) instanceof JumpInstruction jI &&
                jI.code() == Opcode.JUMP &&
                executor.getInstruction(jI.getTarget()) instanceof JumpInstruction jI1 &&
                jI1.code() == Opcode.JUMP
        ) {
            jI.setTarget(jI1.getTarget());
        }
    }
}
