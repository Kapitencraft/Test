package net.kapitencraft.lang.holder.class_ref.generic;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.holder.token.Token;

public class GenericSourceClassReference extends SourceClassReference implements Holder.Validateable {
    public GenericSourceClassReference(Token nameToken, ClassReference reference) {
        super(nameToken.lexeme(), "", nameToken, reference);
    }

    @Override
    public void validate(Compiler.ErrorLogger logger) {
        //always exists
    }
}
