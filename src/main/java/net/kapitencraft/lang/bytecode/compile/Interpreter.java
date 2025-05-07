package net.kapitencraft.lang.bytecode.compile;

import java.util.Scanner;

public class Interpreter {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNextLine()) {
            interpret(scanner.nextLine());
        }
    }

    private static void interpret(String s) {
        Compiler.compile(s);
    }
}
