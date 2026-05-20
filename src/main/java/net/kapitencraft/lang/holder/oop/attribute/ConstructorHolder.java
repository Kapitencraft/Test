package net.kapitencraft.lang.holder.oop.attribute;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceReference;
import net.kapitencraft.lang.holder.oop.AnnotationObj;
import net.kapitencraft.lang.holder.oop.Validatable;
import net.kapitencraft.lang.holder.oop.generic.Generics;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.tool.Pair;

import java.util.List;

public record ConstructorHolder(AnnotationObj[] annotations, Generics generics, Token name, Token closeBracket,
                                List<Pair<SourceReference, String>> params, Token[] body) implements Validatable {
    public void validate(Compiler.ErrorStorage logger) {
        Validatable.validateNullable(annotations, logger);
        if (annotations != null) for (AnnotationObj obj : annotations) obj.validate(logger);
        params.forEach(p -> p.getFirst().validate(logger));
    }

    public List<Pair<ClassReference, String>> extractParams() {
        return this.params.stream().map(p -> p.mapFirst(SourceReference::getReference)).toList();
    }
}
