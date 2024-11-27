package net.kapitencraft.math;

public class BitOperationMain {

    public static void main(String[] args) {
        int a = 124;
        int b = 91;

        long wive = wive(a, b);
        System.out.println(printBinary(wive));
        System.out.println(extract(wive, true));
        System.out.println(extract(wive, false));
    }

    public static long wive(int a, int b) {
        long o = 0;
        for (byte b1 = 0; b1 < 64; b1++) {
            if (b1 % 2 == 0) {
                o = (o << 1 | (a >> b1 / 2) & 1);
            } else {
                o = (o << 1 | (b >> b1 / 2) & 1);
            }
        }
        return o;
    }

    public static int extract(long in, boolean l) {
        int out = 0;
        for (int i = l ? 1 : 0; i < 64; i+=2) {
            out = (int) (out << 1 | (in >> i) & 1);
        }
        return out;
    }

    public static String printBinary(long in) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            builder.append(in >> i & 1);
        }
        return builder.toString();
    }

    public static String obfuscate(String in) {
        char[] chars = new char[in.length()];
        for (int i = 0; i < chars.length; i++) {
            chars[in.length() - 1 - i] = (char) flip(in.charAt(i));
        }
        return new String(chars);
    }

    public static int flip(int in) {
        int out = 0;
        for (int i = 0; i < 32; i++) {
            out = out << 1 | (in >> (32 - i) & 1);
        }
        return out;
    }

    public static byte flip(byte in) {
        byte out = 0;
        for (int i = 0; i < 8; i++) {
            out = (byte) (out << 1 | (in >> (8 - i) & 1));
        }
        return out;
    }

    public static short flip(short in) {
        short out = 0;
        for (int i = 0; i < 32; i++) {
            out = (short) (out << 1 | (in >> (32 - i) & 1));
        }
        return out;
    }

    public static long flip(long in) {
        long out = 0;
        for (int i = 0; i < 32; i++) {
            out = out << 1 | (in >> (32 - i) & 1);
        }
        return out;
    }

}