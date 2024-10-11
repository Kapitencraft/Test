package net.kapitencraft.lang.exception.runtime;

import net.kapitencraft.lang.oop.ClassInstance;

public class LoxException extends RuntimeException {
    private final ClassInstance exceptionInstance;

    public LoxException(ClassInstance exceptionInstance) {
        this.exceptionInstance = exceptionInstance;
    }

    public ClassInstance getExceptionInstance() {
        return exceptionInstance;
    }
}
