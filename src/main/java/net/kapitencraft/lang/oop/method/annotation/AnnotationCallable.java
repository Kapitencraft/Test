package net.kapitencraft.lang.oop.method.annotation;

import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import org.jetbrains.annotations.Nullable;

public class AnnotationCallable implements ScriptedCallable {
    private final ClassReference type;
    private final @Nullable Object value;
    private final ClassReference declaring;

    public AnnotationCallable(ClassReference type, @Nullable Object value, ClassReference declaring) {
        this.type = type;
        this.value = value;
        this.declaring = declaring;
    }

    @Override
    public ClassReference retType() {
        return type;
    }

    protected Object value() {
        return value;
    }

    @Override
    public ClassReference[] argTypes() {
        return new ClassReference[0];
    }

    @Override
    public Object call(Object[] arguments) {
        return value;
    }

    @Override
    public boolean isAbstract() {
        return value == null;
    }

    @Override
    public boolean isFinal() {
        return true;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public ClassReference[] thrown() {
        return new ClassReference[0];
    }

    @Override
    public ClassReference declaringClass() {
        return declaring;
    }
}