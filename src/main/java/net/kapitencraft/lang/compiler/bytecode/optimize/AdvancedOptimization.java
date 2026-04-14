package net.kapitencraft.lang.compiler.bytecode.optimize;

public interface AdvancedOptimization {

    void optimize(BytecodeOptimizer.OptimizationStorage instructions);
}
