package net.kapitencraft.lang.exe.natives.scripted.lang;

import net.kapitencraft.lang.exe.Interpreter;
import net.kapitencraft.lang.exe.natives.NativeClass;
import net.kapitencraft.lang.exe.natives.Rename;

@NativeClass(pck = "scripted.lang")
public class System {

    @Rename("print")
    public static void println(Object in) {
        Interpreter.output.accept(Interpreter.stringify(in));
    }

    public static int time() {
        return (int) Interpreter.elapsedMillis();
    }
}
