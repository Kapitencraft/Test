package net.kapitencraft.lang.holder.oop;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.class_ref.SourceReference;
import net.kapitencraft.lang.holder.token.Token;

public record AnnotationObj(SourceReference type, Token[] properties) implements Validatable {
    @Override
    public void validate(Compiler.ErrorStorage logger) {
        type.validate(logger);
    }
}
