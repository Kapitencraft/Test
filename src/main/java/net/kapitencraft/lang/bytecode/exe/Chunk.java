package net.kapitencraft.lang.bytecode.exe;

import java.util.ArrayList;

public class Chunk {
    public final byte[] code, constants;

    public Chunk(byte[] code, byte[] constants) {
        this.code = code;
        this.constants = constants;
    }

    public static class Builder {
        private final ArrayList<Byte> code, constants;

        public Builder() {
            this.code = new ArrayList<>();
            this.constants = new ArrayList<>();
        }

        public void jumpElse(Runnable ifTrue, Runnable ifFalse) {
            addCode(Opcode.JUMP_IF_FALSE);
            int truePatch = this.code.size();
            ifTrue.run();
            addCode(Opcode.JUMP);
            int falsePatch = this.code.size();
            patchJump(truePatch, (short) falsePatch);
            ifFalse.run();
            patchJump(falsePatch, (short) this.code.size());
        }

        public void patchJump(int index, short destination) {
            this.code.set(index, (byte) (destination & 255));
            this.code.set(index + 1, (byte) ((destination >> 8) & 255));
        }

        public void patchJumpCurrent(int index) {
            int current = this.code.size();
            patchJump(index, (short) current);
        }

        public void jump(Runnable toSkip) {
            int reference = addJump();
            toSkip.run();
            this.code.add(reference, (byte) this.code.size());
        }

        public void addIntConstant(int constant) {
            this.addCode(Opcode.I_CONST);
            this.code.add((byte) this.constants.size());
            for (int i = 0; i < 4; i++) {
                this.constants.add((byte) (constant >> (8 * i) & 255));
            }
        }

        public void addDoubleConstant(double constant) {
            this.addCode(Opcode.D_CONST);
            this.code.add((byte) this.constants.size());
            long l = Double.doubleToLongBits(constant);
            for (int i = 0; i < 8; i++) {
                this.constants.add((byte) (l >> (8 * i) & 255));
            }
        }

        public void addStringConstant(String constant) {
            this.addCode(Opcode.S_CONST);
            this.code.add((byte) this.constants.size());
            for (byte b : constant.getBytes()) {
                this.constants.add(b);
            }
            this.constants.add((byte) '\0');
        }

        public Chunk build() {
            this.addCode(Opcode.RETURN);
            byte[] code = new byte[this.code.size()];
            for (int i = 0; i < code.length; i++) {
                code[i] = this.code.get(i);
            }
            byte[] constants = new byte[this.constants.size()];
            for (int i = 0; i < constants.length; i++) {
                constants[i] = this.constants.get(i);
            }
            return new Chunk(code, constants);
        }

        public void addCode(Opcode opcode) {
            this.code.add((byte) opcode.ordinal());
        }

        public int currentCodeIndex() {
            return this.code.size();
        }

        public int addJumpIfFalse() {
            this.addCode(Opcode.JUMP_IF_FALSE);
            return currentCodeIndex();
        }

        public int addJump() {
            this.addCode(Opcode.JUMP);
            int index = currentCodeIndex();
            this.code.add((byte) 0);
            this.code.add((byte) 0);
            return index;
        }
    }
}
