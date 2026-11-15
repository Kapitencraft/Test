package net.kapitencraft.lang;

import java.util.function.Supplier;

public class BytecodeTest {
    public static void test(Supplier<String> messageSupplier) {
        System.out.println(messageSupplier.get());
    }

    public static void main(String[] args) {
        test(() -> "a");
        test(() -> "b");
        String c = "c";
        test(() -> c);
        test(BytecodeTest::stringGetter);
    }

    private static String stringGetter() {
        return "string";
    }
}
