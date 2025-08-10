package net.kapitencraft.lang.bytecode.exe;

import com.google.gson.JsonObject;
import net.kapitencraft.tool.GsonHelper;

import java.util.ArrayList;

public record Chunk(byte[] code, byte[] constants) {

    public JsonObject save() {
        JsonObject object = new JsonObject();
        object.addProperty("code", encode(this.code));
        object.addProperty("constants", encode(this.constants));
        return object;
    }

    private static String encode(byte[] in) {
        char[] chars = new char[in.length];
        for (int i = 0; i < in.length; i++) {
            chars[i] = (char) in[i];
        }
        return new String(chars);
    }

    private static byte[] decode(String in) {
        char[] chars = in.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte) (chars[i] & 255);
        }
        return bytes;
    }

    public static Chunk load(JsonObject object) {
        byte[] code = decode(GsonHelper.getAsString(object, "code"));
        byte[] constants = decode(GsonHelper.getAsString(object, "constants"));
        return new Chunk(code, constants);
    }

    public static class Builder {
        private final ArrayList<Byte> code, constants;

        public Builder() {
            this.code = new ArrayList<>();
            this.constants = new ArrayList<>();
        }

        public void jumpElse(Runnable ifTrue, Runnable ifFalse) {
            int truePatch = addJumpIfFalse();
            ifTrue.run();
            int falsePatch = addJump();
            patchJump(truePatch, (short) currentCodeIndex());
            ifFalse.run();
            patchJump(falsePatch, (short) currentCodeIndex());
        }

        public void patchJump(int index, short destination) {
            this.code.set(index, (byte) ((destination >> 8) & 255));
            this.code.set(index + 1, (byte) (destination & 255));
        }

        public void patchJumpCurrent(int index) {
            int current = this.code.size();
            patchJump(index, (short) current);
        }

        public void jump(Runnable toSkip) {
            int reference = addJump();
            toSkip.run();
            patchJumpCurrent(reference);
        }

        public void addIntConstant(int constant) {
            this.addCode(Opcode.I_CONST);
            this.add2bArg(this.constants.size());
            for (int i = 0; i < 4; i++) {
                this.constants.add((byte) ((constant >> (8 * i)) & 255));
            }
        }

        public void addDoubleConstant(double constant) {
            this.addCode(Opcode.D_CONST);
            this.add2bArg((byte) this.constants.size());
            long l = Double.doubleToLongBits(constant);
            for (int i = 0; i < 8; i++) {
                this.constants.add((byte) ((l >> (8 * i)) & 255));
            }
        }

        public void addStringConstant(String constant) {
            this.addCode(Opcode.S_CONST);
            injectString(constant);
        }

        public void injectString(String constant) {
            this.add2bArg(this.constants.size());
            this.constants.add((byte) constant.length());
            for (byte b : constant.getBytes()) {
                this.constants.add(b);
            }
        }

        public Chunk build() {
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

        public void addArg(byte b) {
            this.code.add(b);
        }

        public void addArg(int i) {
            this.addArg((byte) (i & 255));
        }

        public void add2bArg(int size) {
            this.addArg((size >> 8) & 255);
            this.addArg(size & 255);
        }

        public void addCode(Opcode opcode) {
            this.addArg(opcode.ordinal());
        }

        public int currentCodeIndex() {
            return this.code.size();
        }

        public int addJumpIfFalse() {
            this.addCode(Opcode.JUMP_IF_FALSE);
            int index = currentCodeIndex();
            this.addArg(0);
            this.addArg(0);
            return index;
        }

        public int addJump() {
            this.addCode(Opcode.JUMP);
            int index = currentCodeIndex();
            this.addArg(0);
            this.addArg(0);
            return index;
        }

        public void clear() {
            this.code.clear();
            this.constants.clear();
        }
    }
}
