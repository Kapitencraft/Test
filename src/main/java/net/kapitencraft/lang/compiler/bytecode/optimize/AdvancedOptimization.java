package net.kapitencraft.lang.compiler.bytecode.optimize;

import net.kapitencraft.lang.compiler.bytecode.instruction.Instruction;

import java.util.List;

public interface AdvancedOptimization {

    void optimize(List<Instruction> instructions);
}
