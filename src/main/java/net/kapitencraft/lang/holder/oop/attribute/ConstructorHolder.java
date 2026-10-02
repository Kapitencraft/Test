package net.kapitencraft.lang.holder.oop.attribute;

import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceReference;
import net.kapitencraft.lang.holder.oop.AnnotationObj;
import net.kapitencraft.lang.holder.oop.Validatable;
import net.kapitencraft.lang.holder.oop.generic.Generics;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.tool.Pair;

import java.util.List;

public record ConstructorHolder(AnnotationObj[] annotations, Generics generics, Token name, Token closeBracket,
                                List<Pair<SourceReference, String>> params, List<SourceReference> thrown,
                                Token[] body) implements OperationHolder {
    public void validate(ErrorStorage logger) {
        Validatable.validateNullable(annotations, logger);
        params.forEach(p -> p.first().validate(logger));
        thrown.forEach(s -> s.validate(logger));
    }

    @Override
    public ClassReference retType() {
        return VarTypeManager.VOID.reference();
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public int modifiers() {
        return 0;
    }
}
