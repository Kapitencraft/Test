package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.holder.class_ref.generic.GenericStack;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

public class SourceClassReference implements Holder.Validateable {
    private final Token nameToken;
    private final ClassReference reference;
    private final String name;

    protected SourceClassReference(String name, Token nameToken, ClassReference reference) {
        this.name = name;
        this.nameToken = nameToken;
        this.reference = reference;
    }

    public ScriptedClass get(GenericStack generics) {
        return reference.get(generics);
    }

    public void setTarget(ScriptedClass target) {
        reference.setTarget(target);
    }

    public static SourceClassReference from(Token name, ClassReference other) {
        return new SourceClassReference(other.name(), name, other);
    }

    public String absoluteName() {
        return reference.absoluteName();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + this.name + (exists() ? ", applied:" + this.reference.get() : "");
    }

    public Token getToken() {
        return nameToken;
    }

    public ClassReference getReference() {
        return reference;
    }

    public void validate(Compiler.ErrorLogger logger) {
        if (!reference.exists())
            logger.errorF(nameToken, "unknown class '%s'", reference.absoluteName());
    }

    public boolean exists() {
        return reference.exists();
    }
}
