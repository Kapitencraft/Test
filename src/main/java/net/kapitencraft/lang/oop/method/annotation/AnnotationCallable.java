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
    public List<ClassReference> argTypes() {
        return List.of();
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

    public static AnnotationCallable intType(@Nullable Integer value) {
        return new AnnotationCallable(ClassReference.of(VarTypeManager.INTEGER), value);
    }

    public static AnnotationCallable doubleType(@Nullable Double value) {
        return new AnnotationCallable(ClassReference.of(VarTypeManager.DOUBLE), value);
    }

    public static AnnotationCallable boolType(@Nullable Boolean value) {
        return new AnnotationCallable(ClassReference.of(VarTypeManager.BOOLEAN), value);
    }

    public static AnnotationCallable stringType(@Nullable String value) {
        return new AnnotationCallable(ClassReference.of(VarTypeManager.STRING.get()), value);
    }

    public static AnnotationCallable enumType(ClassReference type, ClassInstance value) { //TODO fix
        return new AnnotationCallable(type, value);
    }

    public static AnnotationCallable enumType(ClassReference type) {
        return new AnnotationCallable(type, null);
    }
}