package net.kapitencraft.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.lang.compiler.bytecode.optimize.BytecodeOptimizer;
import net.kapitencraft.lang.compiler.bytecode.optimize.Optimization;
import net.kapitencraft.lang.compiler.bytecode.optimize.SimpleOptimization;

public class MergeEqualInstructionOptimization implements Optimization {

    @Override
    public void optimize(BytecodeOptimizer.OptimizationStorage instructions) {
        int i = 0;
        while (i + 1 < instructions.size()) {
            if (instructions.getInstruction(i).equals(instructions.getInstruction(i + 1))) {
                instructions.removeInstruction(i);
            } else
                i++;
        }
    }
}
