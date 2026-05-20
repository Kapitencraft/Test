package net.kapitencraft.lang.holder.oop;

import net.kapitencraft.lang.compiler.Compiler;

public interface Validatable {

    static <T extends Validatable> void validateNullable(T[] validateable, Compiler.ErrorStorage logger) {
        if (validateable != null) for (T obj : validateable) obj.validate(logger);
    }

    void validate(Compiler.ErrorStorage logger);
}
