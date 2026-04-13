package net.kapitencraft.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.lang.compiler.bytecode.instruction.CodeInstruction;
import net.kapitencraft.lang.compiler.bytecode.instruction.Instruction;
import net.kapitencraft.lang.compiler.bytecode.optimize.AdvancedOptimization;
import net.kapitencraft.lang.exe.Opcode;

import java.util.ArrayList;
import java.util.List;

/**
 * removes pure (not modifying IO / calling other methods) instructions whose results will be removed from the stack due to {@code POP} or {@code POP_2} instructions
 * <br>does the same with instructions ending in a {@code RETURN} (not {@code RETURN_ARG}!)
 */
public class RemoveIgnoredResultInstructionsOptimization implements AdvancedOptimization {

    //1. remove non-pure Instructions before RETURN
    //2. remove non-pure Instructions before POP and POP_2 and remove these if finding a DUP instruction
    @Override
    public void optimize(List<Instruction> instructions) {
        int i = instructions.size() - 1;
        while (i >= 0) {
            if (instructions.get(i) instanceof CodeInstruction cI) {
                if (cI.code() == Opcode.RETURN) {
                    i--;
                    while (i >= 0 && instructions.get(i) instanceof CodeInstruction cI1 && cI1.code().isPure()) {
                        instructions.remove(i);
                        i--;
                    }
                } else if (cI.code() == Opcode.POP || cI.code() == Opcode.POP_2) {
                    List<Integer> popLocations = new ArrayList<>();
                    popLocations.add(i);
                    i--;
                    while (i >= 0 && !popLocations.isEmpty() && (instructions.get(i) instanceof CodeInstruction cI1 && cI1.code().isPure())) {
                        switch (cI1.code()) {
                            case POP, POP_2 -> {
                                popLocations.add(i);
                                i--;
                                continue;
                            }
                            case DUP -> {
                                Integer last = popLocations.getLast();
                                CodeInstruction instruction = (CodeInstruction) instructions.get(last);
                                if (instruction.code() == Opcode.POP_2) {
                                    instructions.set(last, CodeInstruction.POP);
                                } else {
                                    instructions.remove(i);
                                    popLocations.removeLast();
                                    popLocations.replaceAll(integer -> integer - 1);
                                }
                            }
                        }
                        instructions.remove(i);
                        i--;
                    }
                }
            }
        }
    }
}
