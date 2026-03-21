package net.kapitencraft.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.lang.compiler.bytecode.instruction.CodeInstruction;
import net.kapitencraft.lang.compiler.bytecode.instruction.JumpInstruction;
import net.kapitencraft.lang.compiler.bytecode.optimize.BytecodeOptimizer;
import net.kapitencraft.lang.compiler.bytecode.optimize.SimpleOptimization;
import net.kapitencraft.lang.exe.Opcode;

/**
 * replaces {@code JUMP_IF_FALSE} instructions with pure {@code JUMP} instruction if the original is prefixed by a {@code FALSE} instruction
 */
public class JumpIfFalseAfterFalseOptimization implements SimpleOptimization {
    @Override
    public void tryExecute(BytecodeOptimizer.Executor executor, int index) {
        if (executor.getInstruction(index) instanceof CodeInstruction cI && cI.code() == Opcode.FALSE &&
                executor.getInstruction(index + 1) instanceof JumpInstruction jI && jI.code() == Opcode.JUMP_IF_FALSE
        ) {
            JumpInstruction instruction = new JumpInstruction(Opcode.JUMP);
            instruction.setTarget(jI.getTarget());
            executor.replaceInstruction(index, jI);
        }
    }
}
