package net.kapitencraft.scripted.lang.oop.method;

import net.kapitencraft.scripted.lang.compiler.Modifiers;
import net.kapitencraft.scripted.lang.compiler.analyser.FinalsPopulatedAnalyser;
import net.kapitencraft.scripted.lang.compiler.analyser.SemanticAnalyser;
import net.kapitencraft.scripted.lang.compiler.ast_optimize.ASTOptimizer;
import net.kapitencraft.scripted.lang.compiler.bytecode.CacheBuilder;
import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.func.ScriptedCallable;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.bytecode.Chunk;
import net.kapitencraft.scripted.lang.holder.bytecode.LineNumberTable;
import net.kapitencraft.scripted.lang.holder.bytecode.LocalVariableTable;
import net.kapitencraft.scripted.lang.holder.bytecode.annotation.Annotation;
import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class CompileCallable implements ScriptedCallable {
    private final ClassReference retType;
    private final List<Pair<ClassReference, String>> params;
    private final ClassReference[] thrown;
    private final List<Stmt> body;
    private final int modifiers;
    private final Annotation[] annotations;
    private Chunk code;
    private LineNumberTable lineNumberTable;
    private LocalVariableTable localVariableTable;
    private final ClassReference declaring;

    public CompileCallable(ClassReference retType, List<Pair<ClassReference, String>> params, ClassReference[] thrown, List<Stmt> body, int modifiers, Annotation[] annotations, ClassReference declaring) {
        this.retType = retType;
        this.params = params;
        this.thrown = thrown;
        this.body = body;
        this.modifiers = modifiers;
        this.annotations = annotations;
        this.declaring = declaring;
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
                chunkBuilder.addLocal(rIndex + i, param.first(), param.second());
            }
            for (Stmt compileStmt : body) {
                builder.cache(compileStmt);
            }
            builder.build(chunkBuilder);
            this.code = chunkBuilder.build();
            this.lineNumberTable = chunkBuilder.getLineNumbers();
            this.localVariableTable = chunkBuilder.getLocals();
        }
    }

    public void analyseSemantics(SemanticAnalyser analyser, ClassReference declaring, Token name) {
        if (!isAbstract()) {
            analyser.clear();
            analyser.analyseBody(body, name, this.retType, this.thrown, params, declaring, isStatic() ? null : declaring);
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
    public int modifiers() {
        return modifiers;
    }

    @Override
    public ClassReference declaringClass() {
        return this.declaring;
    }

    @Override
    public ClassReference retType() {
        return retType;
    }

    @Override
    public ClassReference[] argTypes() {
        return params.stream().map(Pair::first).toArray(ClassReference[]::new);
    }

    @Override
    public ClassReference[] thrown() {
        return thrown;
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

    public void optimize() {
        if (!this.isAbstract())
            ASTOptimizer.optimize(this.body);
    }

    public void checkFinalsPopulated(FinalsPopulatedAnalyser finalsPopulatedAnalyser) {
        for (Stmt stmt : this.body) {
            finalsPopulatedAnalyser.analyse(stmt);
        }
    }
}