package net.kapitencraft.lang.run.natives.scripted.lang;

import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.natives.NativeClass;
import net.kapitencraft.lang.run.natives.Rename;

@NativeClass(pck = "scripted.lang")
public class System {

    @Rename("print")
    public static void println(Object in) {
        Interpreter.INSTANCE.output.accept(Interpreter.stringify(in));
    }

    public static int time() {
        return (int) Interpreter.INSTANCE.elapsedMillis();
    }
}
