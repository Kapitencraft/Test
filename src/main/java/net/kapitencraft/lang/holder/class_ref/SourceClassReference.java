package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;

public class SourceClassReference extends ClassReference {
    private final Token nameToken;
    private final ClassReference reference;

    private SourceClassReference(String name, String pck, Token nameToken, ClassReference reference) {
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

    public Token getToken() {
        return nameToken;
    }
}
