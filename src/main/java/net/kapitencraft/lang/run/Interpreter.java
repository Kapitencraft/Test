package net.kapitencraft.lang.run;

import net.kapitencraft.lang.exception.runtime.AbstractScriptedException;
import net.kapitencraft.lang.holder.ast.RuntimeExpr;
import net.kapitencraft.lang.holder.ast.RuntimeStmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.exception.CancelBlock;
import net.kapitencraft.lang.exception.EscapeLoop;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;
import net.kapitencraft.lang.run.natives.NativeClassLoader;
import net.kapitencraft.lang.tool.Math;
import net.kapitencraft.tool.Pair;

import java.util.*;
import java.util.function.Consumer;

//will use VirtualMachine instead
public class Interpreter {

    public static Consumer<String> output = System.out::println;

    public static final Scanner in = new Scanner(System.in);

    public static boolean suppressClassLoad = false;

    public static long millisAtStart;

    public static void start() {
        millisAtStart = System.currentTimeMillis();
    }

    public static String stringify(Object object) {
        return object == null ? "null" : object.toString();
    }

    public static long elapsedMillis() {
        return System.currentTimeMillis() - millisAtStart;
    }
}
