package net.kapitencraft.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.lang.compiler.bytecode.instruction.CodeInstruction;
import net.kapitencraft.lang.compiler.bytecode.instruction.JumpInstruction;
import net.kapitencraft.lang.compiler.bytecode.optimize.BytecodeOptimizer;
import net.kapitencraft.lang.compiler.bytecode.optimize.SimpleOptimization;
import net.kapitencraft.lang.exe.Opcode;

/**
 * inlines {@code RETURN} or {@code RETURN_ARG} instructions where they are targeted by {@code JUMP} instructions
 */
public class JumpReturnMergeOptimization implements SimpleOptimization {
    @Override
    public void tryExecute(BytecodeOptimizer.Executor executor, int index) {
        if (executor.getInstruction(index) instanceof JumpInstruction jI && jI.code() == Opcode.JUMP &&
                executor.getInstruction(jI.getTarget()) instanceof CodeInstruction cI && (cI.code() == Opcode.RETURN || cI.code() == Opcode.RETURN_ARG)) {
            executor.replaceInstruction(index, new CodeInstruction(cI.code()));
        }
    }
}
