package net.kapitencraft.lang;

import net.kapitencraft.lang.bytecode.exe.VirtualMachine;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.run.load.ClassLoader;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\n");
        String o;
        while (!"exit".equals(o = scanner.next())) {
            switch (o) {
                case "compile" -> Compiler.main(args);
                case "load" -> ClassLoader.main(args);
                default -> System.out.println("\u001B[31munknown argument: '" + o + "'\u001B[0m");
            }
        }
    }
}
