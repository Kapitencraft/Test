package net.kapitencraft.lang.exception.runtime;

import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;

/**
 * base RuntimeException for Scripted mod; caught
 */
public class AbstractScriptedException extends RuntimeException {
    public final ClassInstance exceptionType;

    public AbstractScriptedException(ClassInstance exceptionType) {
        this.exceptionType = exceptionType;
    }

    public static AbstractScriptedException createException(LoxClass clazz, String msg) {
        return new AbstractScriptedException(clazz.createNativeInst(List.of(msg), 0, Interpreter.INSTANCE));
    }
}
