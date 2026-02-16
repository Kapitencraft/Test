package net.kapitencraft.lang.compiler;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.compiler.instruction.Instruction;
import net.kapitencraft.lang.compiler.instruction.JumpInstruction;
import net.kapitencraft.lang.compiler.instruction.JumpTargetInstruction;

import java.util.ArrayList;
import java.util.List;

public class CodeBuilder {
    private final List<Instruction> instructions = new ArrayList<>();

    public void addInstruction(Opcode opcode) {
        instructions.add(new Instruction(opcode));
    }

    public int addJump() {
        int i = instructions.size();
        instructions.add(new JumpInstruction(Opcode.JUMP));
        return i;
    }

    public int addJumpIfFalse() {
        int i = instructions.size();
        instructions.add(new JumpInstruction(Opcode.JUMP_IF_FALSE));
        return i;
    }

    public void patchJump(int jumpID) {
        ((JumpInstruction) this.instructions.get(jumpID)).setIndex(instructions.size());
        instructions.add(new JumpTargetInstruction(jumpID));
    }

    public void jumpElse(Runnable ifTrue, Runnable ifFalse) {
        int truePatch = addJumpIfFalse();
        ifTrue.run();
        int falsePatch = addJump();
        patchJump(truePatch);
        ifFalse.run();
        patchJump(falsePatch);
    }
}
