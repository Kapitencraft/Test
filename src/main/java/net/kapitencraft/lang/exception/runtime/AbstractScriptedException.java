package net.kapitencraft.lang.exception.runtime;

import net.kapitencraft.lang.oop.clazz.inst.AbstractClassInstance;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;

import java.util.List;
import java.util.function.Supplier;

/**
 * base RuntimeException for Scripted mod; caught
 */
public class AbstractScriptedException extends RuntimeException {
    public final AbstractClassInstance exceptionType;

    public AbstractScriptedException(AbstractClassInstance exceptionType) {
        this.exceptionType = exceptionType;
    }

    public static AbstractScriptedException createException(Supplier<LoxClass> clazz, String msg) {
        return new AbstractScriptedException(clazz.get().createNativeInst(List.of(msg), 0, Interpreter.INSTANCE));
    }
}
