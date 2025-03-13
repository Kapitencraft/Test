package net.kapitencraft.lang.oop.method.annotation;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AnnotationCallable implements ScriptedCallable {
    private final ClassReference type;
    private final @Nullable Object value;

    public AnnotationCallable(ClassReference type, @Nullable Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public ClassReference type() {
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
    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
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
}