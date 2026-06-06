package net.kapitencraft.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.lang.compiler.bytecode.optimize.AdvancedOptimization;
import net.kapitencraft.lang.compiler.bytecode.optimize.BytecodeOptimizer;

public class MergeSumLoopsOptimization implements AdvancedOptimization {
    //int sum = 0;
    //for (int i = 0; i <= n; i++) {
    //  sum += i;
    //}
    //->
    //sum = i * (i + 1) / 2

    //int sum = 0;
    //for (int i = 0; i <= n; i++) {
    //  sum += i ** 2;
    //}
    //->
    //sum = n * (n + 1) * (2n + 1)

    @Override
    public void optimize(BytecodeOptimizer.OptimizationStorage instructions) {

    }
}
