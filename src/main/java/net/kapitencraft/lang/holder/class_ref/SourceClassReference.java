package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

public class SourceClassReference extends ClassReference implements Holder.Validateable {
    private final Token nameToken;
    private final ClassReference reference;

    protected SourceClassReference(String name, String pck, Token nameToken, ClassReference reference) {
        super(name, pck);
        this.nameToken = nameToken;
        this.reference = reference;
    }

    @Override
    public ScriptedClass get() {
        return reference.get();
    }

    @Override
    public void setTarget(ScriptedClass target) {
        reference.setTarget(target);
    }

    public static SourceClassReference from(Token name, ClassReference other) {
        return new SourceClassReference(other.name(), other.pck(), name, other);
    }

    @Override
    public String absoluteName() {
        return reference.absoluteName();
    }

    @Override
    public String toString() {
        return "SourceClassReference@" + this.name() + (exists() ? ", applied:" + this.reference.get() : "");
    }

    public Token getToken() {
        return nameToken;
    }

    public void validate(Compiler.ErrorLogger logger) {
        if (!reference.exists())
            logger.errorF(nameToken, "unknown class '%s'", reference.absoluteName());
    }

    @Override
    public boolean exists() {
        return reference.exists();
    }
}
