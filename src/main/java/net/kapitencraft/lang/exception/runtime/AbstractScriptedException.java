package net.kapitencraft.lang.exception.runtime;

import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;
import java.util.function.Supplier;

/**
 * base RuntimeException for Scripted mod; caught
 */
public class AbstractScriptedException extends RuntimeException {
    public final ClassInstance exceptionType;

    public AbstractScriptedException(ClassInstance exceptionType) {
        this.exceptionType = exceptionType;
    }

    public static AbstractScriptedException createException(Supplier<ScriptedClass> clazz, String msg) {
        return new AbstractScriptedException(clazz.get().createNativeInst(new Object[]{msg}, 0));
    }
}
