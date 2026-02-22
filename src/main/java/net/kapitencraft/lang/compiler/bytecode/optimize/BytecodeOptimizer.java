package net.kapitencraft.lang.compiler.bytecode.optimize;

import net.kapitencraft.lang.compiler.bytecode.instruction.Instruction;

import java.util.List;

public class BytecodeOptimizer {
    public static final BytecodeOptimizer INSTANCE = new BytecodeOptimizer();

    private final List<Optimization> optimizations = List.of(
            //TODO add
    );

    public void optimize(List<Instruction> instructions) {
        Executor executor = new Executor(instructions);
        for (Optimization optimization : optimizations) {
        }
    }

    public static class Executor {
        private final List<Instruction> instructions;

        private Executor(List<Instruction> instructions) {
            this.instructions = instructions;
        }
    }
}
