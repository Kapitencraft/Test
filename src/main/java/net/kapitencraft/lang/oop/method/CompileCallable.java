package net.kapitencraft.lang.oop.method;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.bytecode.exe.Chunk;
import net.kapitencraft.lang.compiler.CacheBuilder;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.oop.clazz.inst.CompileAnnotationClassInstance;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class CompileCallable implements ScriptedCallable {
    private final ClassReference retType;
    private final List<? extends Pair<? extends ClassReference, String>> params;
    private final Stmt[] body;
    private final short modifiers;
    private final CompileAnnotationClassInstance[] annotations;

    public CompileCallable(ClassReference retType, List<? extends Pair<? extends ClassReference, String>> params, Stmt[] body, short modifiers, CompileAnnotationClassInstance[] annotations) {
        this.retType = retType;
        this.params = params;
        this.body = body;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    public JsonObject save(CacheBuilder builder) {
        JsonObject object = new JsonObject();
        object.addProperty("retType", retType.absoluteName());
        {
            JsonArray array = new JsonArray();
            params.stream().map(Pair::left).map(ClassReference::absoluteName).forEach(array::add);
            object.add("params", array);
        }
        if (!Modifiers.isAbstract(modifiers)) {
            Chunk.Builder chunk = builder.setup();
            for (Stmt compileStmt : body) {
                builder.cache(compileStmt);
            }
            object.add("body", chunk.build().save());
        }
        if (this.modifiers != 0) object.addProperty("modifiers", this.modifiers);

        object.add("annotations", builder.cacheAnnotations(this.annotations));
        return object;
    }

    @Override
    public Object call(Object[] arguments) {
        throw new IllegalAccessError("can not run Compile Callable!");
    }

    @Override
    public boolean isAbstract() {
        return body == null;
    }

    @Override
    public boolean isFinal() {
        return Modifiers.isFinal(modifiers);
    }

    @Override
    public boolean isStatic() {
        return Modifiers.isStatic(modifiers);
    }

    @Override
    public ClassReference type() {
        return retType;
    }

    @Override
    public ClassReference[] argTypes() {
        return params.stream().map(Pair::left).toArray(ClassReference[]::new);
    }
}