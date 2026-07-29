package net.kapitencraft.lang.holder.bytecode;

import net.kapitencraft.lang.compiler.bytecode.CacheBuffer;
import net.kapitencraft.lang.compiler.bytecode.ConstantPoolBuilder;
import net.kapitencraft.lang.exe.Opcode;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.exe.load.BytecodeReader;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantDoubleInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantFloatInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantIntegerInfo;
import net.kapitencraft.lang.holder.bytecode.const_pool.ConstantStringInfo;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;

import java.util.ArrayList;
import java.util.List;

public record Chunk(byte[] code, ExceptionHandler[] handlers, LineNumberTable lineNumberTable,
                    LocalVariableTable localVariableTable) {

    /**
     * a builder for the chunk, used inside {@link net.kapitencraft.lang.compiler.bytecode.CacheBuilder CacheBuilder} to create json format of the chunk
     */
    public static class Builder {
        private final List<ExceptionHandler> handlers;
        private final LineNumberTable.Builder lineNumbers;
        private final LocalVariableTable.Builder locals;
        private final ArrayList<Byte> code;
        private final ConstantPoolBuilder cp;

        public Builder(ConstantPoolBuilder cp) {
            this.cp = cp;
            this.code = new ArrayList<>();
            this.handlers = new ArrayList<>();
            this.lineNumbers = new LineNumberTable.Builder();
            this.locals = new LocalVariableTable.Builder();
        }

        public void jumpElse(Runnable ifTrue, Runnable ifFalse) {
            int truePatch = addJumpIfFalse();
            ifTrue.run();
            int falsePatch = addJump();
            patchJump(truePatch, (short) currentCodeIndex());
            ifFalse.run();
            patchJump(falsePatch, (short) currentCodeIndex());
        }

        public void addLocal(int index, ClassReference type, String name) {
            this.locals.addLocal(currentCodeIndex(), index, VarTypeManager.getClassName(type), name);
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
            this.cp.addEntry(new ConstantIntegerInfo(constant));
        }

        public void addDoubleConstant(double constant) {
            this.addCode(Opcode.D_CONST);
            this.add2bArg(
                    this.cp.addEntry(new ConstantDoubleInfo(constant))
            );
        }

        public void addFloatConstant(float v) {
            this.addCode(Opcode.F_CONST);
            this.add2bArg(
                    this.cp.addEntry(new ConstantFloatInfo(v))
            );
        }

        public void addStringConstant(String constant) {
            this.addCode(Opcode.S_CONST);
            injectString(constant);
        }

        public void injectString(String constant) {
            this.add2bArg(
                    this.cp.addEntry(new ConstantStringInfo(constant))
            );
        }

        public int injectStringNoArg(String constant) {
            return this.cp.addEntry(new ConstantStringInfo(constant));
        }

        public Chunk build() {
            byte[] code = new byte[this.code.size()];
            for (int i = 0; i < code.length; i++) {
                code[i] = this.code.get(i);
            }
            return new Chunk(code, this.handlers.toArray(new ExceptionHandler[0]), this.lineNumbers.build(), this.locals.build(this.currentCodeIndex()));
        }

        public void addArg(byte b) {
            this.code.add(b);
        }

        public void addArg(int i) {
            this.addArg((byte) (i & 255));
        }

        public void add2bArg(int arg) {
            this.addArg((arg >> 8));
            this.addArg(arg);
        }

        public void add4bArg(int arg) {
            this.addArg((arg >> 24));
            this.addArg((arg >> 16));
            this.addArg((arg >> 8));
            this.addArg(arg);
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
            this.handlers.clear();
            this.lineNumbers.clear();
            this.locals.clear();
        }

        public void addExceptionHandler(int startOp, int endOp, int handlerOp, int catchType) {
            this.handlers.add(new ExceptionHandler(startOp, endOp, handlerOp, catchType));
        }

        public void invokeStatic(String methodSignature) {
            this.addCode(Opcode.INVOKE_STATIC);
            this.injectString(methodSignature);
        }

        public void invokeVirtual(String methodSignature) {
            this.addCode(Opcode.INVOKE_VIRTUAL);
            this.injectString(methodSignature);
        }

        public void addInt(int v) {
            switch (v) {
                case -1 -> addCode(Opcode.I_M1);
                case 0 -> addCode(Opcode.I_0);
                case 1 -> addCode(Opcode.I_1);
                case 2 -> addCode(Opcode.I_2);
                case 3 -> addCode(Opcode.I_3);
                case 4 -> addCode(Opcode.I_4);
                case 5 -> addCode(Opcode.I_5);
                default -> addIntConstant(v);
            }
        }

        public void changeLineIfNecessary(Token type) {
            this.changeLineIfNecessary(type.line());
        }

        public void changeLineIfNecessary(int line) {
            this.lineNumbers.changeIfNecessary(line, this.currentCodeIndex());
        }

        public void addTraceDebug(byte[] ints) {
            this.addCode(Opcode.TRACE);
            this.add2bArg(
                    this.cp.addEntry(new Constant())
            );
        }
    }

    /**
     * @param startOp   the start ip
     * @param endOp     the end ip
     * @param handlerOp the code executed when this handler matches the thrown exception
     * @param catchType the type of error to be caught
     */
    public record ExceptionHandler(int startOp, int endOp, int handlerOp, int catchType) {

        public void toStream(CacheBuffer buffer) {
            buffer.writeShort(startOp);
            buffer.writeShort(endOp);
            buffer.writeShort(handlerOp);
            buffer.writeShort(catchType);
        }

        public static ExceptionHandler read(BytecodeReader reader) {
            return new ExceptionHandler(
                    reader.read2b(),
                    reader.read2b(),
                    reader.read2b(),
                    reader.read2b()
            );
        }
    }
}
