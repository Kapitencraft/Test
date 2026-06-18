package net.kapitencraft.lang.oop.method;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.bytecode.storage.Chunk;
import net.kapitencraft.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.lang.compiler.Synthesizer;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class CompileCallable implements ScriptedCallable {
    private final ClassReference retType;
    private final List<? extends Pair<? extends ClassReference, String>> params;
    private final Stmt[] body;
    private final short modifiers;
    private final Annotation[] annotations;
    private Chunk code;

    public CompileCallable(ClassReference retType, List<? extends Pair<? extends ClassReference, String>> params, Stmt[] body, short modifiers, Annotation[] annotations) {
        this.retType = retType;
        this.params = params;
        this.body = body;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    public void save(Synthesizer builder) {
        if (!Modifiers.isAbstract(modifiers)) {
            Chunk.Builder chunk = builder.setup();
            for (int i = 0; i < this.params.size(); i++) {
                Pair<? extends ClassReference, String> param = this.params.get(i);
                chunk.addLocal(0, i, param.left(), param.right());
            }
            for (Stmt compileStmt : body) {
                builder.cache(compileStmt);
            }
            this.code = chunk.build();
        }
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
    public short modifiers() {
        return modifiers;
    }

    @Override
    public ClassReference retType() {
        return retType;
    }

    @Override
    public ClassReference[] argTypes() {
        return params.stream().map(Pair::left).toArray(ClassReference[]::new);
    }

    @Override
    public Chunk getChunk() {
        return code;
    }
}