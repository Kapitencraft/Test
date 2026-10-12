package net.kapitencraft.lang.compiler.bytecode.optimize;

public interface Optimization {

    void optimize(BytecodeOptimizer.OptimizationStorage instructions);
}
