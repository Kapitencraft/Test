package net.kapitencraft.lang.oop.method.annotation;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.oop.clazz.inst.AnnotationClassInstance;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;

import java.util.List;

public class GeneratedAnnotationCallable extends AnnotationCallable {
    private final Expr expr;
    private final AnnotationClassInstance[] annotations;

    public GeneratedAnnotationCallable(ClassReference type, Expr expr, AnnotationClassInstance[] annotations) {
        super(type, null);
        this.expr = expr;
        this.annotations = annotations;
    }

    public static GeneratedAnnotationCallable of(JsonObject object) {
        ClassReference target = ClassLoader.loadClassReference(object, "type");
        Expr expr = CacheLoader.readOptionalSubExpr(object, "val");
        AnnotationClassInstance[] annotations = CacheLoader.readAnnotations(object);
        return new GeneratedAnnotationCallable(target, expr, annotations);
    }

    public JsonObject save(CacheBuilder builder) {
        JsonObject object = new JsonObject();
        object.addProperty("type", this.type().absoluteName());
        //if (this.expr != null) object.add("val", builder.cache(this.expr)); TODO
        object.add("annotations", builder.cacheAnnotations(annotations));
        return object;
    }

    @Override
    public boolean isAbstract() {
        return expr == null;
    }

    @Override
    public Object call(Environment environment, Interpreter interpreter, List<Object> arguments) {
        return null; //interpreter.evaluate(expr);
    }
}
