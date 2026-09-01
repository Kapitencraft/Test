package net.kapitencraft.lang.holder.oop.attribute;

import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceReference;
import net.kapitencraft.lang.holder.oop.AnnotationObj;
import net.kapitencraft.lang.holder.oop.Validatable;
import net.kapitencraft.lang.holder.oop.generic.Generics;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.tool.Pair;

import java.util.List;

public record MethodHolder(int modifiers,
                           AnnotationObj[] annotations, Generics generics, SourceReference returnType,
                           Token name, Token closeBracket, List<Pair<SourceReference, String>> params,
                           List<SourceReference> thrown,
                           Token[] body) implements Validatable, OperationHolder {
    public void validate(ErrorStorage logger) {
        Validatable.validateNullable(annotations, logger);
        returnType.validate(logger);
        params.forEach(p -> p.first().validate(logger));
        thrown.forEach(s -> s.validate(logger));
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(modifiers);
    }

    @Override
    public ClassReference retType() {
        return returnType.getReference();
    }
}
