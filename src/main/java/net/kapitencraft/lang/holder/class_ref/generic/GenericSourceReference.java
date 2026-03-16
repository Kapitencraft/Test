package net.kapitencraft.lang.holder.class_ref.generic;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceReference;
import net.kapitencraft.lang.holder.token.Token;

public class GenericSourceReference extends SourceReference {
    public GenericSourceReference(Token nameToken, ClassReference reference) {
        super(nameToken.lexeme(), nameToken, reference);
    }

    @Override
    public void validate(Compiler.ErrorStorage logger) {
        //always exists
    }
}
