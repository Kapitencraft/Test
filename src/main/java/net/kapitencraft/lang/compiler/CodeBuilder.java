package net.kapitencraft.lang.compiler;

import net.kapitencraft.lang.bytecode.exe.Opcode;
import net.kapitencraft.lang.compiler.instruction.*;
import net.kapitencraft.lang.compiler.instruction.constant.IntegerConstantInstruction;
import net.kapitencraft.lang.holder.token.Token;

import java.util.ArrayList;
import java.util.List;

public class CodeBuilder {
    private final List<Instruction> instructions = new ArrayList<>();

    public void changeLineIfNecessary(Token obj) {
        add(new ChangeLineInstruction(obj.line()));
    }

    public void addSimple(Opcode opcode) {
        add(new SimpleInstruction(opcode));
    }

    public void addStringInstruction(Opcode opcode, String val) {
        add(new StringArgInstruction(opcode, val));
    }

    public int addJump() {
        int i = instructions.size();
        add(new JumpInstruction(Opcode.JUMP));
        return i;
    }

    public int addJumpIfFalse() {
        int i = instructions.size();
        add(new JumpInstruction(Opcode.JUMP_IF_FALSE));
        return i;
    }

    public void patchJump(int jumpID) {
        add(new JumpTargetInstruction(jumpID));
        ((JumpableInstruction) this.instructions.get(jumpID)).setTarget(instructions.size());
    }

    public void jumpElse(Runnable ifTrue, Runnable ifFalse) {
        int truePatch = addJumpIfFalse();
        ifTrue.run();
        int falsePatch = addJump();
        patchJump(truePatch);
        ifFalse.run();
        patchJump(falsePatch);
    }

    public void addJumpMultiTargetInstruction(List<Integer> origins) {
        add(new JumpMultiTargetInstruction(origins));
        int target = this.instructions.size();
        for (Integer origin : origins) {
            ((JumpableInstruction) this.instructions.get(origin)).setTarget(target);
        }
    }

    public int size() {
        return instructions.size();
    }

    public void addSwitch(int size, List<SwitchInstruction.Entry> entries) {
        add(new SwitchInstruction(size, entries));
    }

    public void add(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public void addInt(int v) {
        add(switch (v) {
            case -1 -> new SimpleInstruction(Opcode.I_M1);
            case 0 -> new SimpleInstruction(Opcode.I_0);
            case 1 -> new SimpleInstruction(Opcode.I_1);
            case 2 -> new SimpleInstruction(Opcode.I_2);
            case 3 -> new SimpleInstruction(Opcode.I_3);
            case 4 -> new SimpleInstruction(Opcode.I_4);
            case 5 -> new SimpleInstruction(Opcode.I_5);
            default -> new IntegerConstantInstruction(v);
        });
    }

    public void addGet(int i) {
        add(new GetInstruction(i));
    }
}
