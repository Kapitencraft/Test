package net.kapitencraft.lang.holder.oop.attribute;


import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.class_ref.SourceReference;
import net.kapitencraft.lang.holder.oop.AnnotationObj;
import net.kapitencraft.lang.holder.oop.Validatable;
import net.kapitencraft.lang.holder.token.Token;

public record FieldHolder(short modifiers, AnnotationObj[] annotations, SourceReference type, Token name, Token assign,
                          Token[] body) implements Validatable {
    @Override
    public void validate(Compiler.ErrorStorage logger) {
        Validatable.validateNullable(annotations, logger);
        type.validate(logger);
    }
}
