package net.kapitencraft.tool;

public class TestRun {

    public static void main(String[] args) {
        System.out.println(fib(32));
    }

    public static int fib(int i) {
        if (i <= 2) return 1;
        return fib(i - 2) + fib(i - 1);
    }
}
