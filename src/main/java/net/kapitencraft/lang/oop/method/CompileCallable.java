package net.kapitencraft.lang.oop.method;

import net.kapitencraft.lang.bytecode.storage.Chunk;
import net.kapitencraft.lang.bytecode.storage.annotation.Annotation;
import net.kapitencraft.lang.compiler.Synthesizer;
import net.kapitencraft.lang.compiler.Modifiers;
import net.kapitencraft.lang.exe.ScriptedCallable;
import net.kapitencraft.lang.exe.VarTypeManager;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.bytecode.Chunk;
import net.kapitencraft.lang.holder.bytecode.LineNumberTable;
import net.kapitencraft.lang.holder.bytecode.LocalVariableTable;
import net.kapitencraft.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class CompileCallable implements ScriptedCallable {
    private final ClassReference retType;
    private final List<? extends Pair<? extends ClassReference, String>> params;
    private final Stmt[] body;
    private final short modifiers;
    private final Annotation[] annotations;
    private Chunk code;
    private LineNumberTable lineNumberTable;
    private LocalVariableTable localVariableTable;

    public CompileCallable(ClassReference retType, List<? extends Pair<? extends ClassReference, String>> params, Stmt[] body, short modifiers, Annotation[] annotations) {
        this.retType = retType;
        this.params = params;
        this.body = body;
        this.modifiers = modifiers;
        this.annotations = annotations;
    }

    public void build(CacheBuilder builder) {
        if (!isAbstract()) {
            Chunk.Builder chunkBuilder = builder.reset();
            int rIndex = 0;
            if (!isStatic()) {
                chunkBuilder.addLocal(0, VarTypeManager.VOID.reference(), "this");
                rIndex++;
            }
            for (int i = 0; i < this.params.size(); i++) {
                Pair<? extends ClassReference, String> param = this.params.get(i);
                chunkBuilder.addLocal(0, i, param.left(), param.right());
            }
            for (Stmt compileStmt : body) {
                builder.cache(compileStmt);
            }
            builder.build(chunkBuilder);
            this.code = chunkBuilder.build();
            this.lineNumberTable = chunkBuilder.getLineNumbers();
            this.localVariableTable = chunkBuilder.getLocals();
        }
        if (this.modifiers != 0) object.addProperty("modifiers", this.modifiers);

        object.add("annotations", builder.cacheAnnotations(this.annotations));
        return object;
    }

    public void analyseSemantics(SemanticAnalyser analyser, ClassReference declaring) {
        if (!isAbstract())
            analyser.analyseBody(body, this.retType, this.thrown, params, isStatic() ? null : declaring);
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

    public LineNumberTable getLNT() {
        return this.lineNumberTable;
    }

    public LocalVariableTable getLVT() {
        return this.localVariableTable;
    }

    @Override
    public Chunk getChunk() {
        return code;
    }

    public LineNumberTable getLNT() {
        return this.lineNumberTable;
    }

    public LocalVariableTable getLVT() {
        return this.localVariableTable;
    }
}