package net.kapitencraft.lang.oop.method.annotation;

import com.google.gson.JsonObject;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;

public class CompileAnnotationCallable extends AnnotationCallable {
    private final Expr expr;
    private final CompileAnnotationClassInstance[] annotations;

    public CompileAnnotationCallable(ClassReference type, Expr expr, CompileAnnotationClassInstance[] annotations) {
        super(type, null);
        this.expr = expr;
        this.annotations = annotations;
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
    public Object call(Object[] arguments) {
        throw new IllegalAccessError("can not call compile annotation method");
    }
}
