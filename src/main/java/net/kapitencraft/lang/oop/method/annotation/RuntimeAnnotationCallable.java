package net.kapitencraft.lang.oop.method.annotation;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.ast.RuntimeExpr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.inst.RuntimeAnnotationClassInstance;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;

import java.util.List;

public class RuntimeAnnotationCallable extends AnnotationCallable {
    private final RuntimeExpr expr;
    private final RuntimeAnnotationClassInstance[] annotations;

    public RuntimeAnnotationCallable(ClassReference type, RuntimeExpr expr, RuntimeAnnotationClassInstance[] annotations) {
        super(type, null);
        this.expr = expr;
        this.annotations = annotations;
    }

    public static RuntimeAnnotationCallable of(JsonObject object) {
        ClassReference target = ClassLoader.loadClassReference(object, "type");
        RuntimeExpr expr = CacheLoader.readOptionalSubExpr(object, "val");
        RuntimeAnnotationClassInstance[] annotations = CacheLoader.readAnnotations(object);
        return new RuntimeAnnotationCallable(target, expr, annotations);
    }

    @Override
    public boolean isAbstract() {
        return expr == null;
    }

    @Override
    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
        return interpreter.evaluate(expr);
    }
}
