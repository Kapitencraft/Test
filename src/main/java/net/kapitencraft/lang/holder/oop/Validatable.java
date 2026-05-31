package net.kapitencraft.lang.holder.oop;

import net.kapitencraft.lang.compiler.error.ErrorStorage;

public interface Validatable {

    static <T extends Validatable> void validateNullable(T[] validateable, ErrorStorage logger) {
        if (validateable != null) for (T obj : validateable) obj.validate(logger);
    }

    void validate(ErrorStorage logger);
}
