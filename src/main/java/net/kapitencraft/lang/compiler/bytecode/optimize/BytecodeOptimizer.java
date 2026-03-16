package net.kapitencraft.lang.compiler.bytecode.optimize;

import net.kapitencraft.lang.compiler.bytecode.instruction.Instruction;
import net.kapitencraft.lang.compiler.bytecode.optimize.impl.JumpIfFalseAfterFalseOptimization;
import net.kapitencraft.lang.compiler.bytecode.optimize.impl.JumpMergeOptimization;
import net.kapitencraft.lang.compiler.bytecode.optimize.impl.JumpReturnMergeOptimization;
import net.kapitencraft.lang.compiler.bytecode.optimize.impl.RemoveUnreachableOpcodesOptimization;

import java.util.List;

public class BytecodeOptimizer {
    public static final BytecodeOptimizer INSTANCE = new BytecodeOptimizer();

    private final List<SimpleOptimization> simpleOptimizations = List.of(
            //TODO add
            new JumpMergeOptimization(), //merge jumps pointing to jumps
            new JumpIfFalseAfterFalseOptimization(), //merge jump_if_false directly after false
            new JumpReturnMergeOptimization() //replace jump with return if jump points at return
    );

    private final List<AdvancedOptimization> advancedOptimizations = List.of(
            //backtrack unused pure instructions before POP or POP2
            new RemoveUnreachableOpcodesOptimization() //remove unreachable opcodes caused by other optimizations
    );

    public void optimize(List<Instruction> instructions) {
        Executor executor = new Executor(instructions);
        for (SimpleOptimization optimization : simpleOptimizations) {
            for (int i = 0; i < instructions.size(); i++) {
                optimization.tryExecute(executor, i);
            }
        }
        for (AdvancedOptimization optimization : advancedOptimizations) {
            optimization.optimize(instructions);
        }
    }

    public static class Executor {
        private final List<Instruction> instructions;

        private Executor(List<Instruction> instructions) {
            this.instructions = instructions;
        }

        public Instruction getInstruction(int index) {
            return this.instructions.get(index);
        }

        public void replaceInstruction(int index, Instruction instruction) {
            this.instructions.set(index, instruction);
        }
    }
}
