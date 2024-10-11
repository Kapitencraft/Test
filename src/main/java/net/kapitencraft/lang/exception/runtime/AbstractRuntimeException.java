package net.kapitencraft.lang.exception.runtime;

import net.kapitencraft.lang.oop.ClassInstance;

/**
 * base RuntimeException for Scripted mod; caught
 */
public class AbstractRuntimeException extends RuntimeException {
    public final ClassInstance exceptionType;

    public AbstractRuntimeException(ClassInstance exceptionType) {
        this.exceptionType = exceptionType;
    }
}
