package net.kapitencraft.lang.holder.oop.attribute;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceReference;
import net.kapitencraft.lang.holder.oop.AnnotationObj;
import net.kapitencraft.lang.holder.oop.Validatable;
import net.kapitencraft.lang.holder.oop.generic.Generics;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.tool.Pair;

import java.util.List;

public interface OperationHolder extends Validatable {
    ClassReference retType();

    Generics generics();

    Token[] body();

    boolean isStatic();

    short modifiers();

    Token closeBracket();

    AnnotationObj[] annotations();

    default List<Pair<ClassReference, String>> extractParams() {
        return this.params().stream().map(p -> p.mapFirst(SourceReference::getReference)).toList();
    }

    default ClassReference[] extractThrown() {
        return thrown().stream().map(SourceReference::getReference).toArray(ClassReference[]::new);
    }

    Token name();

    List<Pair<SourceReference, String>> params();

    List<SourceReference> thrown();
}
