package net.kapitencraft.lang.holder.class_ref.generic;

import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import org.jetbrains.annotations.Nullable;

public class AppliedGenericsReference extends SourceClassReference {
    private final Holder.Generics generics;
    private final ClassReference reference;

    public AppliedGenericsReference(ClassReference reference, Holder.Generics generics, Token name) {
        super(reference.name(), reference.pck(), name, reference);
        this.generics = generics;
        this.reference = reference;
    }

    @Override
    public boolean exists() {
        return reference.exists();
    }

    @Override
    public boolean is(ScriptedClass scriptedClass) {
        return reference.is(scriptedClass);
    }

    @Override
    public ScriptedClass get(@Nullable GenericStack generics) {
        return reference.get(generics);
    }

    public void push(GenericStack genericStack) {
        this.generics.pushToStack(genericStack);
    }
}
