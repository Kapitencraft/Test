package net.kapitencraft.lang.holder.class_ref;

import net.kapitencraft.lang.compiler.VarTypeParser;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ClassType;

public class SourceClassReference extends ClassReference {
    private final Token nameToken;

    public SourceClassReference(String name, String pck, ClassType type, Token nameToken) {
        super(name, pck, type);
        this.nameToken = nameToken;
    }

    public static SourceClassReference from(Token name, ClassReference other) {
        SourceClassReference r = new SourceClassReference(other.name(), other.pck(), other.getType(), name);
        r.target = other.target;
        return r;
    }

    public Token getToken() {
        return nameToken;
    }
}
