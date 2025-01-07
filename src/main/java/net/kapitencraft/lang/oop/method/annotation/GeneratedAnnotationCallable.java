package net.kapitencraft.lang.oop.method.annotation;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.load.CacheLoader;
import net.kapitencraft.lang.run.load.ClassLoader;

import java.util.List;

public class GeneratedAnnotationCallable extends AnnotationCallable {
    private final Expr expr;

    public GeneratedAnnotationCallable(ClassReference type, Expr expr) {
        super(type, null);
        this.expr = expr;
    }

    public static GeneratedAnnotationCallable of(JsonObject object) {
        ClassReference target = ClassLoader.loadClassReference(object, "type");
        Expr expr = null;
        if (object.has("val")) expr = CacheLoader.readSubExpr(object, "val");
        return new GeneratedAnnotationCallable(target, expr);
    }

    public JsonObject save(CacheBuilder builder) {
        JsonObject object = new JsonObject();
        object.addProperty("type", this.type().absoluteName());
        if (this.expr != null) object.add("val", builder.cache(this.expr));
        return object;
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
