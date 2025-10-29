package net.kapitencraft.lang.bytecode.exe;

import net.kapitencraft.lang.exception.runtime.AbstractScriptedException;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.clazz.inst.DynamicClassInstance;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.natives.NativeClassInstance;
import net.kapitencraft.lang.run.natives.NativeClassLoader;
import net.kapitencraft.tool.StringReader;
import org.jetbrains.annotations.Contract;

import java.util.*;

public class VirtualMachine {
    public static boolean DEBUG;

    private static final Object[] stack = new Object[1024];
    private static int stackIndex = 0;
    private static final CallFrame[] callStack = new CallFrame[256];
    private static int callStackTop = 0;

    private static CallFrame frame;

    private static final class CallFrame {
        private final String signature;
        private final ScriptedCallable callable;
        private final byte[] code, constants;
        private final int stackBottom;
        private final Chunk.ExceptionHandler[] handlers;
        private int ip;

        private CallFrame(String signature, ScriptedCallable callable, int stackBottom) {
            this.signature = signature;
            this.callable = callable;
            this.stackBottom = stackBottom;
            Chunk chunk = callable.getChunk();
            this.code = chunk.code();
            this.constants = chunk.constants();
            this.handlers = chunk.handlers();
        }

        @Override
        public String toString() {
            return "Frame[" +
                    "callable=" + callable + ", " +
                    "index=" + stackBottom + ", " +
                    "signature=" + signature + ']';
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
        Optional.ofNullable(target.getMethod("main([Lscripted/lang/String;)"))
                .ifPresentOrElse(method -> {
                    if (!method.isStatic()) {
                        System.err.println("Non-static method can not be referenced from a static context");
                        return;
                    }
                    pushCall(new CallFrame(VarTypeManager.getClassName(target) + "main", method, 0));
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
                        System.out.println("current ip: " + frame.ip);
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
            try {
                func:
                while (frame.ip < frame.code.length) {
                    Opcode o = Opcode.byId(readByte());
                    if (DEBUG) System.out.printf("[DEBUG]: Executing %s\n", o);
                    switch (o) {
                        //region control-flow
                        case POP -> stackIndex--; //pops the highest stack element
                        case POP_2 -> stackIndex -= 2; //pops the 2 highest stack elements
                        case DUP -> stack[stackIndex] = stack[stackIndex++ - 1]; //duplicates the highest stack element
                        case DUP2 -> { //duplicates the 2 highest stack element, retaining order (a, b -> a, b, a, b)
                            stack[stackIndex] = stack[stackIndex++ - 2];
                            stack[stackIndex] = stack[stackIndex++ - 2];
                        }
                        case DUP_X1 -> { //duplicates the highest stack element one element down (a, b -> b, a, b)
                            Object obj = stack[stackIndex - 1]; //get stack top
                            stack[stackIndex - 1] = stack[stackIndex - 2]; //replace stack top with element below
                            stack[stackIndex - 2] = obj; //replace element below with stack top
                            stack[stackIndex++] = obj; //add stack top on top
                        }
                        case DUP_X2 -> { //duplicates the highest stack element 2 elements down (a, b, c -> c, a, b, c)
                            Object obj = stack[stackIndex - 1];
                            stack[stackIndex - 1] = stack[stackIndex - 2];
                            stack[stackIndex - 2] = stack[stackIndex - 3];
                            stack[stackIndex - 3] = obj;
                            stack[stackIndex++] = obj;
                        }
                        case DUP2_X1 -> { //duplicates the highest 2 stack elements one element down (a, b, c -> b, c, a, b, c)
                            Object obj = stack[stackIndex - 2];
                            Object obj1 = stack[stackIndex - 1];
                            stack[stackIndex - 1] = stack[stackIndex - 3];
                            stack[stackIndex - 3] = obj;
                            stack[stackIndex - 2] = obj1;
                            stack[stackIndex++] = obj;
                            stack[stackIndex++] = obj1;
                        }
                        case DUP2_X2 -> { //duplicates the highest 2 stack elements 2 elements down (a, b, c, d -> c, d, a, b, c, d)
                            Object obj = stack[stackIndex - 2];
                            Object obj1 = stack[stackIndex - 1];
                            stack[stackIndex - 2] = stack[stackIndex - 4];
                            stack[stackIndex - 1] = stack[stackIndex - 3];
                            stack[stackIndex - 4] = obj;
                            stack[stackIndex - 3] = obj1;
                            stack[stackIndex++] = obj;
                            stack[stackIndex++] = obj1;
                        }
                        //endregion
                        case INVOKE_STATIC -> {
                            String execute = constString(frame.constants, read2Byte());
                            StringReader reader = new StringReader(execute);
                            ScriptedClass type = VarTypeManager.flatParse(reader);
                            if (invokeStaticInitIfNecessary(type, 3)) continue;
                            ScriptedCallable callable = type.getMethod(reader.getRemaining());

                            int length = callable.argTypes().length;
                            if (!callable.isStatic()) length++; //remove callee too
                            int callableStackTop = stackIndex - length;

                            if (callable.isNative()) {
                                Object[] args = new Object[length];
                                System.arraycopy(stack, callableStackTop, args, 0, length);
                                stackIndex = callableStackTop; //reset stack index
                                push(callable.call(args));
                            } else
                                pushCall(new CallFrame(execute, callable, callableStackTop));
                        }
                        case INVOKE_VIRTUAL -> {
                            String execute = constString(frame.constants, read2Byte());
                            StringReader reader = new StringReader(execute);
                            ScriptedClass type = VarTypeManager.flatParse(reader);
                            if (invokeStaticInitIfNecessary(type, 3)) continue;

                            ScriptedCallable referenceCallable = type.getMethod(reader.getRemaining());

                            int length = referenceCallable.argTypes().length + 1; //reference object
                            int callableStackTop = stackIndex - length;
                            ClassInstance instance = (ClassInstance) stack[callableStackTop];
                            ScriptedCallable callable = instance.getType().getMethod(reader.getRemaining()); //virtual invoke of the method

                            if (callable.isNative()) {
                                Object[] args = new Object[length];
                                System.arraycopy(stack, callableStackTop, args, 0, length);
                                stackIndex = callableStackTop; //reset stack index
                                push(callable.call(args));
                            } else
                                pushCall(new CallFrame(execute, callable, callableStackTop));
                        }
                        case THROW -> {
                            if (!handleException((ClassInstance) pop())) return;
                        }
                        case NEW -> {
                            ScriptedClass reference = VarTypeManager.directFlatParse(constString(frame.constants, read2Byte()));
                            push(new DynamicClassInstance(reference));
                        }
                        case IA_NEW -> push(new int[(int) pop()]);
                        case DA_NEW -> push(new double[(int) pop()]);
                        case CA_NEW -> push(new char[(int) pop()]);
                        case FA_NEW -> push(new float[(int) pop()]);
                        case RA_NEW -> push(new Object[(int) pop()]);
                        case RETURN -> {
                            break func;
                        }
                        case SLICE -> slice();
                        case JUMP -> frame.ip = read2Byte();
                        case JUMP_IF_FALSE -> {
                            if (!(boolean) pop()) frame.ip = read2Byte();
                            else frame.ip += 2;
                        }
                        case ARRAY_LENGTH -> push(arrayLength(pop()));
                        case GET -> get(readByte());
                        case GET_0 -> get(0);
                        case GET_1 -> get(1);
                        case GET_2 -> get(2);
                        case ASSIGN -> assign(readByte());
                        case ASSIGN_0 -> assign(0);
                        case ASSIGN_1 -> assign(1);
                        case ASSIGN_2 -> assign(2);
                        case NULL -> push(null);
                        case TRUE -> push(true);
                        case FALSE -> push(false);
                        case I_M1 -> push(-1);
                        case I_0 -> push(0);
                        case I_1 -> push(1);
                        case I_2 -> push(2);
                        case I_3 -> push(3);
                        case I_4 -> push(4);
                        case I_5 -> push(5);
                        case D_1 -> push(1d);
                        case D_M1 -> push(-1d);
                        case F_1 -> push(1f);
                        case F_M1 -> push(-1f);
                        case I_CONST -> push(constInt(frame.constants, read2Byte()));
                        case D_CONST -> push(constDouble(frame.constants, read2Byte()));
                        case F_CONST -> push(constFloat(frame.constants, read2Byte()));
                        case S_CONST -> push(NativeClassLoader.wrapString(constString(frame.constants, read2Byte())));
                        case CONCENTRATION ->
                                push(NativeClassLoader.wrapString(pop() + (String) ((NativeClassInstance) pop()).getObject()));
                        case I_NEGATION -> push(-(int) pop());
                        case D_NEGATION -> push(-(double) pop());
                        case F_NEGATION -> push(-(float) pop());
                        case I_POW -> push((int) Math.pow((int) pop(), (int) pop()));
                        case D_POW -> push(Math.pow((double) pop(), (double) pop()));
                        case F_POW -> push((float) Math.pow((float) pop(), (float) pop()));
                        case I_ADD -> push((int) pop() + (int) pop());
                        case D_ADD -> push((double) pop() + (double) pop());
                        case F_ADD -> push((float) pop() + (float) pop());
                        case I_DIV -> push((int) pop() / (int) pop());
                        case D_DIV -> push((double) pop() / (double) pop());
                        case F_DIV -> push((float) pop() / (float) pop());
                        case I_MUL -> push((int) pop() * (int) pop());
                        case D_MUL -> push((double) pop() * (double) pop());
                        case F_MUL -> push((float) pop() * (float) pop());
                        case I_SUB -> push((int) pop() - (int) pop());
                        case D_SUB -> push((double) pop() - (double) pop());
                        case F_SUB -> push((float) pop() - (float) pop());
                        case IA_LOAD -> push(((int[]) pop())[(int) pop()]);
                        case DA_LOAD -> push(((double[]) pop())[(int) pop()]);
                        case CA_LOAD -> push(((char[]) pop())[(int) pop()]);
                        case FA_LOAD -> push(((float[]) pop())[(int) pop()]);
                        case RA_LOAD -> push(((Object[]) pop())[(int) pop()]);
                        case IA_STORE -> ((int[]) pop())[(int) pop()] = (int) pop();
                        case DA_STORE -> ((double[]) pop())[(int) pop()] = (double) pop();
                        case CA_STORE -> ((char[]) pop())[(int) pop()] = (char) pop();
                        case FA_STORE -> ((float[]) pop())[(int) pop()] = (float) pop();
                        case RA_STORE -> ((Object[]) pop())[(int) pop()] = pop();
                        case EQUAL -> push(pop() == pop());
                        case NEQUAL -> push(pop() != pop());
                        case I_GEQUAL -> push((int) pop() >= (int) pop());
                        case D_GEQUAL -> push((double) pop() >= (double) pop());
                        case F_GEQUAL -> push((float) pop() >= (float) pop());
                        case I_LEQUAL -> push((int) pop() <= (int) pop());
                        case D_LEQUAL -> push((double) pop() <= (double) pop());
                        case F_LEQUAL -> push((float) pop() <= (float) pop());
                        case I_GREATER -> push((int) pop() > (int) pop());
                        case D_GREATER -> push((double) pop() > (double) pop());
                        case F_GREATER -> push((float) pop() > (float) pop());
                        case I_LESSER -> push((int) pop() < (int) pop());
                        case D_LESSER -> push((double) pop() < (double) pop());
                        case F_LESSER -> push((float) pop() < (float) pop());
                        case NOT -> push(!(boolean) pop());
                        case OR -> push((boolean) pop() || (boolean) pop());
                        case AND -> push((boolean) pop() && (boolean) pop());
                        case XOR -> push((boolean) pop() ^ (boolean) pop());
                        case D2F -> push((float) (double) pop());
                        case SWITCH -> {
                        }
                        case GET_FIELD -> {
                            ClassInstance instance = (ClassInstance) pop();
                            if (invokeStaticInitIfNecessary(instance.getType(), 1)) continue;
                            String s = constString(frame.constants, read2Byte());
                            push(instance.getField(s));
                        }
                        case GET_STATIC -> {
                            String c = constString(frame.constants, read2Byte());
                            ScriptedClass scriptedClass = VarTypeManager.directFlatParse(c);
                            if (invokeStaticInitIfNecessary(scriptedClass, 3)) continue;
                            String field = constString(frame.constants, read2Byte());
                            scriptedClass.getStaticField(field);
                        }
                        case PUT_FIELD -> {
                            ClassInstance instance = (ClassInstance) pop();
                            if (invokeStaticInitIfNecessary(instance.getType(), 1)) continue;
                            String s = constString(frame.constants, read2Byte());
                            instance.assignField(s, pop());
                        }
                        case PUT_STATIC -> {
                            String c = constString(frame.constants, read2Byte());
                            ScriptedClass scriptedClass = VarTypeManager.directFlatParse(c);
                            if (invokeStaticInitIfNecessary(scriptedClass, 3)) continue;
                            String field = constString(frame.constants, read2Byte());
                            scriptedClass.setStaticField(field, pop());
                        }
                        default -> throw new IllegalArgumentException("unknown opcode: " + o);
                    }
                }
                if (--callStackTop > 0) {
                    popCall();
                }
            } catch (Throwable t) {
                if (!handleException(createException(VarTypeManager.UNKNOWN_ERROR, t.getMessage()))) return;
            }
        }
    }

    private static final Set<ScriptedClass> initialized = new HashSet<>();

    private static boolean invokeStaticInitIfNecessary(ScriptedClass scriptedClass, int opcodeOffset) {
        if (scriptedClass.isNative() || initialized.contains(scriptedClass)) return false;
        initialized.add(scriptedClass); //add it before so it doesn't create a recursion loop when a static call / get is executed from within the <clinit> method
        ScriptedCallable method = scriptedClass.getMethod("<clinit>()");
        if (method != null) { //TODO fix frame being damaged when static init is called
            frame.ip -= opcodeOffset; //reset to the last invoked Opcode, to prevent ip corruption
            pushCall(new CallFrame(VarTypeManager.getClassName(scriptedClass) + "<clinit>", method, stackIndex));
            return true;
        }
        return false;
    }

    private static ClassInstance createException(ClassReference type, String message) {
        DynamicClassInstance instance = new DynamicClassInstance(type.get());
        instance.assignField("message", message);
        return instance;
    }

    private static ClassInstance createClassCast(ScriptedClass reference1, ClassReference reference2) {
        return createException(VarTypeManager.CLASS_CAST_EXCEPTION, reference1.absoluteName() + " can not be converted to " + reference2.absoluteName());
    }

    private static boolean handleException(ClassInstance exception) {
        ScriptedClass type = exception.getType();
        if (!type.isChildOf(VarTypeManager.THROWABLE.get())) {
            return handleException(createClassCast(type, VarTypeManager.THROWABLE));
        }

        List<String> stackTrace = new ArrayList<>();
        for (int i = callStackTop - 1; i > -1; i--) {
            stackTrace.add(callStack[i].signature);
        }

        while (callStackTop > 0) {
            int ip = frame.ip;
            for (Chunk.ExceptionHandler handler : frame.handlers) {
                if (ip >= handler.startOp() && ip < handler.endOp()) {
                    if (handler.catchType() != 0) {
                        ClassReference reference = VarTypeManager.parseType(new StringReader(constString(frame.constants, handler.catchType())));
                        if (!reference.get().isParentOf(type)) continue;
                    }
                    push(exception);
                    frame.ip = handler.handlerOp();
                    return true;
                }
            }
            popCall(); //pop call when no possible exception handler could be found
            callStackTop--;
        }
        System.out.printf("Caused by: %s\n", exception.getField("message"));
        stackTrace.forEach(s -> System.out.println("\tat " + s));
        return false;
        //TODO exit thread
    }

    private static <T> void slice() {
        Integer rawInterval = (Integer) pop();
        Integer rawEnd = (Integer) pop();
        Integer rawStart = (Integer) pop();
        T[] array = (T[]) pop();
        int interval = rawInterval != null ? rawInterval : 1;
        int min = rawStart != null ? rawStart : interval < 0 ? array.length : 0;
        int max = rawEnd != null ? rawEnd : interval < 0 ? 0 : array.length;
        T[] out = (T[]) new Object[(max - min) / interval];
        int index = 0;
        for (int i = min; i < max; i+=interval) {
            out[index] = array[i];
            index++;
        }
        push(out);
    }

    private static int arrayLength(Object o) {
        if (o instanceof char[] chars) return chars.length;
        if (o instanceof int[] ints) return ints.length;
        if (o instanceof double[] doubles) return doubles.length;
        if (o instanceof float[] floats) return floats.length;
        if (o instanceof boolean[] booleans) return booleans.length;
        return ((Object[]) o).length;
    }

    private static void get(int i) {
        push(stack[frame.stackBottom + i]);
        if (DEBUG) System.out.printf("[DEBUG]: GET: %s\n", i);
    }

    private static void assign(int i) {
        stack[frame.stackBottom + i] = stack[stackIndex - 1]; //DO NOT MODIFY THE STACK
        if (DEBUG) System.out.printf("[DEBUG]: ASSIGN: %s\n", i);
    }

    private static void popCall() {
        Object o = pop();
        stackIndex = frame.stackBottom;
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

    @Contract(pure = true)
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

    public static float constFloat(byte[] bytes, Integer integer) {
        return Float.intBitsToFloat(constInt(bytes, integer));
    }
}