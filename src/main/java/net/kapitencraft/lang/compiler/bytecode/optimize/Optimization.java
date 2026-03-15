package net.kapitencraft.lang.compiler.bytecode.optimize;

public interface Optimization {

    void tryExecute(BytecodeOptimizer.Executor executor, int index);

    class Simple implements Optimization {
        private final InstructionPattern pattern;

        private Simple(InstructionPattern pattern) {

            this.pattern = pattern;
        }

        @Override
        public void tryExecute(BytecodeOptimizer.Executor executor, int index) {
            if (this.pattern.matches(executor, index)) {

            }
        }
    }

    class Builder {
        private InstructionPattern pattern;


    }
}
