package net.kapitencraft.lang.bytecode.exe;

import net.kapitencraft.lang.exception.runtime.AbstractScriptedException;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.natives.NativeClassLoader;
import net.kapitencraft.tool.StringReader;

import java.util.Arrays;
import java.util.Optional;

public class VirtualMachine {
    public static boolean DEBUG;

    private static final Object[] stack = new Object[1024];
    private static int stackIndex = 0;
    private static final CallFrame[] callStack = new CallFrame[256];
    private static int callStackTop = 0;

    private static CallFrame frame;

    private static final class CallFrame {
        private final ScriptedCallable callable;
        private final byte[] code, constants;
        private final int stackBottom;
        private int ip;

        private CallFrame(ScriptedCallable callable, int stackBottom) {
            this.callable = callable;
            this.stackBottom = stackBottom;
            Chunk chunk = callable.getChunk();
            this.code = chunk.code();
            this.constants = chunk.constants();
        }

        @Override
        public String toString() {
            return "Frame[" +
                    "callable=" + callable + ", " +
                    "index=" + stackBottom + ']';
        }
    }

    public static void main(String[] args) {
        Chunk.Builder builder = new Chunk.Builder();
        builder.addDoubleConstant(15.4);
        builder.addDoubleConstant(7.7);
        builder.addCode(Opcode.D_DIV);
        Chunk chunk = builder.build();
        Disassembler.disassemble(chunk, "test");
    }

    private static int readByte() {
        return frame.code[frame.ip++] & 255;
    }

    private static int read2Byte() {
        return (readByte() << 8) | readByte();
    }

    public static void runMainMethod(ScriptedClass target, String data, boolean profiling, boolean output) {
        if (!target.hasMethod("main")) return;
        Optional.ofNullable(target.getMethod("main", new ClassReference[] {VarTypeManager.STRING.array()}))
                .ifPresentOrElse(method -> {
                    if (!method.isStatic()) {
                        System.err.println("Non-static method can not be referenced from a static context");
                        return;
                    }
                    pushCall(new CallFrame(method, 0));
                    try {
                        Interpreter.start();
                        push(Arrays.stream(data.split(" ")).map(NativeClassLoader::wrapString).toArray());
                        run();
                        if (output) {
                            if (profiling)
                                System.out.println("\u001B[32mExecution took " + Interpreter.elapsedMillis() + "ms\u001B[0m");
                            else System.out.println("\u001B[32mExecution finished\u001B[0m");
                        }
                    } catch (AbstractScriptedException e) {
                        System.err.println("Caused by: " + e.exceptionType.getType().absoluteName() + ": " + e.exceptionType.getField("message"));
                        System.exit(65);
                    } catch (Exception e) {
                        Disassembler.disassemble(frame.callable.getChunk(), "Error");
                        throw e;
                    } finally {
                        callStackTop = 0;
                        stackIndex = 0;
                    }
                }, () -> System.err.printf("could not find executable main method inside class '%s'", target.absoluteName()));
    }

    @SuppressWarnings("ExpressionComparedToItself")
    public static void run() {
        while (callStackTop > 0) {
            func: while (frame.ip < frame.code.length) {
                switch (Opcode.byId(readByte())) {
                    case RETURN -> {
                        break func;
                    }
                    case INVOKE -> {
                        String execute = constString(frame.constants, read2Byte());
                        StringReader reader = new StringReader(execute);
                        ClassReference type = VarTypeManager.parseType(reader);
                        String name = reader.readUntil('(');
                        reader.skip(); //skip (
                        ClassReference[] argTypes = VarTypeManager.parseArgTypes(reader);

                        ScriptedCallable callable = type.get().getMethod(name, argTypes);
                        int length = callable.argTypes().length;
                        if (!callable.isStatic()) length++; //remove callee too
                        int callableStackTop = stackIndex - length;

                        if (callable.isNative()) {
                            Object[] args = new Object[length];
                            System.arraycopy(stack, callableStackTop, args, 0, length);//todo remove parsing types
                            push(callable.call(args));
                        } else
                            pushCall(new CallFrame(callable, callableStackTop)); //TODO fix index
                    }
                    case JUMP -> frame.ip = read2Byte();
                    case JUMP_IF_FALSE -> {
                        if (!(boolean) pop()) frame.ip = read2Byte();
                        else frame.ip += 2;
                    }
                    case GET -> push(stack[frame.stackBottom + readByte()]);
                    case NULL -> push(null);
                    case TRUE -> push(true);
                    case FALSE -> push(false);
                    case I_1 -> push(1);
                    case D_1 -> push(1d);
                    case I_M1 -> push(-1);
                    case D_M1 -> push(-1d);
                    case I_CONST -> push(constInt(frame.constants, read2Byte()));
                    case D_CONST -> push(constDouble(frame.constants, read2Byte()));
                    case S_CONST -> push(constString(frame.constants, read2Byte()));
                    case CONCENTRATION -> push((pop() + (String) pop()));
                    case I_NEGATION -> push(-(int) pop());
                    case D_NEGATION -> push(-(double) pop());
                    case I_POW -> push((int) Math.pow((int) pop(), (int) pop()));
                    case D_POW -> push(Math.pow((double) pop(), (double) pop()));
                    case I_ADD -> push((int) pop() + (int) pop());
                    case D_ADD -> push((double) pop() + (double) pop());
                    case I_DIV -> push((int) pop() / (int) pop());
                    case D_DIV -> push((double) pop() / (double) pop());
                    case I_MUL -> push((int) pop() * (int) pop());
                    case D_MUL -> push((double) pop() * (double) pop());
                    case I_SUB -> push((int) pop() - (int) pop());
                    case D_SUB -> push((double) pop() - (double) pop());
                    case IA_LOAD -> push(((int[]) pop())[(int) pop()]);
                    case DA_LOAD -> push(((double[]) pop())[(int) pop()]);
                    case RA_LOAD -> push(((Object[]) pop())[(int) pop()]);
                    case IA_STORE -> push(((int[]) pop())[(int) pop()] = (int) pop());
                    case DA_STORE -> push(((double[]) pop())[(int) pop()] = (double) pop());
                    case RA_STORE -> push(((Object[]) pop())[(int) pop()] = pop());
                    case I_GEQUAL -> push((int) pop() >= (int) pop());
                    case D_GEQUAL -> push((double) pop() >= (double) pop());
                    case I_LEQUAL -> push((int) pop() <= (int) pop());
                    case D_LEQUAL -> push((double) pop() <= (double) pop());
                    case I_GREATER -> push((int) pop() > (int) pop());
                    case D_GREATER -> push((double) pop() > (double) pop());
                    case I_LESSER -> push((int) pop() < (int) pop());
                    case D_LESSER -> push((double) pop() < (double) pop());
                    case NOT -> push(!(boolean) pop());
                    case OR -> push((boolean) pop() || (boolean) pop());
                    case AND -> push((boolean) pop() && (boolean) pop());
                    case XOR -> push((boolean) pop() ^ (boolean) pop());
                    case D2F -> push((float) (double) pop());
                    default -> throw new IllegalArgumentException("unknown opcode: " + frame.code[frame.ip - 1]);
                }
            }
            if (--callStackTop > 0) {
                popCall();
            }
        }
    }

    private static void popCall() {
        int amount = frame.callable.argTypes().length;
        Object o = pop();
        stackIndex -= amount;
        push(o);
        frame = callStack[callStackTop - 1];
        if (DEBUG) System.out.printf("[DEBUG]: POP_CALL (@%3d): stackIndex=%3d\n", callStackTop, frame.stackBottom);
    }

    private static void pushCall(CallFrame callFrame) {
        frame = callStack[callStackTop++] = callFrame;
        if (DEBUG) System.out.printf("[DEBUG]: PUSH_CALL (@%3d): stackIndex=%3d\n", callStackTop - 1, callFrame.stackBottom);
    }

    private static void push(Object o) {
        stack[stackIndex++] = o;
        if (DEBUG) System.out.printf("[DEBUG]: PUSH (@%3d): %s\n", stackIndex - 1, o);
    }

    private static Object pop() {
        if (DEBUG) System.out.printf("[DEBUG]: POP  (@%3d): %s\n", stackIndex - 1, stack[stackIndex - 1]);
        return stack[--stackIndex];
    }

    public static String constString(byte[] constants, int index) {
        int length = constants[index];
        byte[] s = new byte[length];
        System.arraycopy(constants, index + 1, s, 0, length);
        return new String(s);
    }

    public static double constDouble(byte[] constants, int index) {
        long d = 0;
        for (int i = 0; i < 8; i++) {
            d += (constants[index + i] & 255L) << (8 * i);
        }
        return Double.longBitsToDouble(d);
    }

    public static int constInt(byte[] constants, int index) {
        int c = 0;
        for (int i = 0; i < 4; i++) {
            c += (constants[index + i] & 255) << (8 * i);
        }
        return c;
    }
}