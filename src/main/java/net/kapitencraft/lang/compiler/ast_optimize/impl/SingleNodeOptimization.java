package net.kapitencraft.lang.compiler.ast_optimize.impl;

import net.kapitencraft.lang.compiler.ast_optimize.ASTOptimization;
import net.kapitencraft.lang.holder.ast.Stmt;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public abstract class SingleNodeOptimization<T extends Stmt> implements ASTOptimization {
    private final Class<T> type;

    protected SingleNodeOptimization(Class<T> type) {
        this.type = type;
    }

    @Override
    public void optimize(List<Stmt> code) {
        for (int i = 0; i < code.size(); i++) {
            Stmt stmt = code.get(i);
            if (type.isInstance(stmt)) {
                Stmt ret = optimize((T) stmt);
                if (ret != null) {
                    code.set(i, ret);
                    continue;
                }
            }
            if (stmt instanceof Stmt.If f) {
                optimize(List.of(f.thenBranch));
                Arrays.stream(f.elifs).map(b -> List.of(b.body)).forEach(this::optimize);
                if (f.elseBranch != null)
                    optimize(List.of(f.elseBranch));
            } else if (stmt instanceof Stmt.ForEach forEach) {
                optimize(List.of(forEach.body));
            } else if (stmt instanceof Stmt.For f) {
                optimize(List.of(f.body));
            }
        }
    }

    abstract @Nullable Stmt optimize(T value);
}