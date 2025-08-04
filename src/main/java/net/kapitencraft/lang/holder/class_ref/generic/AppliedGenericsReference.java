package net.kapitencraft.lang.holder.class_ref.generic;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.Holder;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.class_ref.SourceClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import org.jetbrains.annotations.Nullable;

public class AppliedGenericsReference extends ClassReference {
    private final Holder.AppliedGenerics generics;
    private final ClassReference reference;

    public AppliedGenericsReference(ClassReference reference, Holder.AppliedGenerics generics) {
        super(reference.name(), reference.pck());
        this.generics = generics;
        this.reference = reference;
    }

    public void push(GenericStack genericStack, Compiler.ErrorLogger logger) {
        this.generics.applyToStack(genericStack, this.reference.getGenerics(), logger);
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
}
