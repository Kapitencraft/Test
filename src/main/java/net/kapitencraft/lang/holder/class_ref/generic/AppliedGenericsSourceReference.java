package net.kapitencraft.lang.holder.class_ref.generic;

import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceReference;
import net.kapitencraft.lang.holder.token.Token;

public class AppliedGenericsSourceReference extends SourceReference {
    private final Holder.AppliedGenerics generics;

    public static AppliedGenericsSourceReference create(Token nameToken, ClassReference type, Holder.AppliedGenerics generics) {
        return new AppliedGenericsSourceReference(type.name(), nameToken, type, generics);
    }

    protected AppliedGenericsSourceReference(String name, Token nameToken, ClassReference reference, Holder.AppliedGenerics generics) {
        super(name, nameToken, reference);
        this.generics = generics;
    }

    public Holder.AppliedGenerics getGenerics() {
        return generics;
    }
}
