package net.kapitencraft.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.lang.compiler.bytecode.optimize.BytecodeOptimizer;
import net.kapitencraft.lang.compiler.bytecode.optimize.SimpleOptimization;

public class MergeEqualInstructionOptimization implements SimpleOptimization {
    @Override
    public void tryExecute(BytecodeOptimizer.OptimizationStorage optimizationStorage, int index) {
        if (index < optimizationStorage.size() - 1 && optimizationStorage.getInstruction(index).equals(optimizationStorage.getInstruction(index + 1))) {
            optimizationStorage.removeInstruction(index);
        }
    }
}
