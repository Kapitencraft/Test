package net.kapitencraft.lang.oop.method.annotation;

import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class AnnotationCallable implements ScriptedCallable {
    private final LoxClass type;
    private final @Nullable Object value;

    public AnnotationCallable(LoxClass type, @Nullable Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public LoxClass type() {
        return type;
    }

    protected Object value() {
        return value;
    }

    @Override
    public List<? extends LoxClass> argTypes() {
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
        return new AnnotationCallable(VarTypeManager.INTEGER, value);
    }

    public static AnnotationCallable doubleType(@Nullable Double value) {
        return new AnnotationCallable(VarTypeManager.DOUBLE, value);
    }

    public static AnnotationCallable boolType(@Nullable Boolean value) {
        return new AnnotationCallable(VarTypeManager.BOOLEAN, value);
    }

    public static AnnotationCallable stringType(@Nullable String value) {
        return new AnnotationCallable(VarTypeManager.STRING.get(), value);
    }

    public static AnnotationCallable enumType(Supplier<LoxClass> type) { //TODO fix
        return new AnnotationCallable(type.get(), null);
    }
}