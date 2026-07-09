package net.kapitencraft.lang.compiler.ast_optimize;

import net.kapitencraft.lang.compiler.ast_optimize.impl.SumMergeOptimization;
import net.kapitencraft.lang.holder.ast.Stmt;

import java.util.List;

public class ASTOptimizer {
    private static final List<ASTOptimization> optimizations = List.of(
            new SumMergeOptimization()
    );

    public static void optimize(List<Stmt> code) {
        for (ASTOptimization optimization : optimizations) {
            optimization.optimize(code);
        }
    }
}
