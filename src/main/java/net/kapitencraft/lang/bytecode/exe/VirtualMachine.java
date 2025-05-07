package net.kapitencraft.lang.bytecode.exe;

public class VirtualMachine {
    private static int index = 0;
    private static final Object[] stack = new Object[256];
    private static int stackTop = 0;

    public static void main(String[] args) {
        Chunk.Builder builder = new Chunk.Builder();
        builder.addDoubleConstant(15.4);
        builder.addDoubleConstant(7.7);
        builder.addCode(Opcode.D_DIV);
        Chunk chunk = builder.build();
        run(chunk);
        Disassembler.disassemble(chunk, "test");
    }

    private static byte readByte(byte[] code) {
        return code[index++];
    }

    public static void run(Chunk chunk) {
        byte[] code = chunk.code;
        byte[] constants = chunk.constants;
        while (index < code.length) {
            switch (Opcode.byId(readByte(code))) {
                case RETURN -> {
                    System.out.println(pop());
                    return;
                }
                case NULL -> push(null);
                case TRUE -> push(true);
                case FALSE -> push(false);
                case I_CONST -> push(constInt(constants, readByte(code)));
                case D_CONST -> push(constDouble(constants, readByte(code)));
                case S_CONST -> push(constString(constants, readByte(code)));
                case CONCENTRATION -> push((pop() + (String) pop()));
                case I_NEGATION -> push(-(int) pop());
                case D_NEGATION -> push(-(double) pop());
                case I_ADD -> push(((int) pop() + (int) pop()));
                case D_ADD -> push(((double) pop() + (double) pop()));
                case I_DIV -> push(((int) pop() / (int) pop()));
                case D_DIV -> push(((double) pop() / (double) pop()));
                case I_MUL -> push(((int) pop() * (int) pop()));
                case D_MUL -> push(((double) pop() * (double) pop()));
                case I_SUB -> push(((int) pop() - (int) pop()));
                case D_SUB -> push(((double) pop() - (double) pop()));
                case NOT -> push(!(boolean) pop());
                case OR -> push(((boolean) pop() || (boolean) pop()));
                case AND -> push(((boolean) pop() && (boolean) pop()));
                case XOR -> push(((boolean) pop() ^ (boolean) pop()));
                default -> throw new IllegalArgumentException("unknown opcode: " + code[index-1]);
            }
        }
    }

    private static void push(Object o) {
        stack[stackTop++] = o;
    }

    private static Object pop() {
        return stack[--stackTop];
    }

    public static String constString(byte[] bytes, int index) {
        int end = index;
        while (bytes[end] != '\0') end++;
        byte[] s = new byte[end - index];
        System.arraycopy(bytes, index, s, 0, end - index);
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