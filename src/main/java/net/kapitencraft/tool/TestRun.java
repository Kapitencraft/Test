package net.kapitencraft.tool;

public class TestRun {

    public static void main(String[] args) {
        long nanoTime = System.nanoTime();
        System.out.println(fib(32));
        System.out.println("took " + (System.nanoTime() - nanoTime) + "ns");
    }

    public static int fib(int i) {
        if (i <= 2) return 1;
        return fib(i - 2) + fib(i - 1);
    }
}
