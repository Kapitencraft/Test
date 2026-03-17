package net.kapitencraft.lang.compiler.bytecode.optimize.impl;

import net.kapitencraft.lang.compiler.bytecode.instruction.CodeInstruction;
import net.kapitencraft.lang.compiler.bytecode.instruction.Instruction;
import net.kapitencraft.lang.compiler.bytecode.instruction.JumpInstruction;
import net.kapitencraft.lang.compiler.bytecode.instruction.SwitchInstruction;
import net.kapitencraft.lang.compiler.bytecode.optimize.AdvancedOptimization;
import net.kapitencraft.lang.exe.Opcode;

import java.util.ArrayDeque;
import java.util.List;

public class RemoveUnreachableOpcodesOptimization implements AdvancedOptimization {

    @Override
    public void optimize(List<Instruction> instructions) {
        ArrayDeque<Integer> ipQueue = new ArrayDeque<>();
        ipQueue.push(0);
        boolean[] flags = new boolean[instructions.size()];

        while (!ipQueue.isEmpty()) {
            int i = ipQueue.pop();
            while (i < instructions.size()) {
                if (flags[i])
                    break; //check has already happened for these instructions. no need to check again
                flags[i] = true; //mark instruction as reachable
                if (instructions.get(i) instanceof CodeInstruction cI) {
                    if (cI.code() == Opcode.RETURN || cI.code() == Opcode.RETURN_ARG) {
                        break;
                    }
                    if (cI instanceof JumpInstruction jI) {
                        ipQueue.add(jI.getTarget());
                        if (cI.code() == Opcode.JUMP)
                            break;
                    }
                    if (cI instanceof SwitchInstruction switchInstruction) {
                        ipQueue.add(switchInstruction.getTarget());
                        switchInstruction.getEntries().stream().mapToInt(SwitchInstruction.Entry::idx).forEach(ipQueue::add);
                        break;
                    }
                }
                i++;
            }
        }
        for (int i = flags.length - 1; i >= 0; i--) {
            if (!flags[i]) {
                //TODO include exception handlers
                instructions.remove(i); //remove from the back to the front to keep the order aligned
            }
        }
    }
}
