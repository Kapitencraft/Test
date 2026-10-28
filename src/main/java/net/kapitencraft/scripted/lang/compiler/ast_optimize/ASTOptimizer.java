package net.kapitencraft.scripted.lang.compiler.ast_optimize;

import net.kapitencraft.scripted.lang.compiler.ast_optimize.impl.ConstantConditionIfOptimization;
import net.kapitencraft.scripted.lang.compiler.ast_optimize.impl.CleanUnusedLocalsOptimization;
import net.kapitencraft.scripted.lang.compiler.ast_optimize.impl.SumMergeOptimization;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;

import java.util.List;

public class ASTOptimizer {
    private static final List<ASTOptimization> optimizations = List.of(
            new SumMergeOptimization(),
            new ConstantConditionIfOptimization(),
            new CleanUnusedLocalsOptimization()
    );

    public static void optimize(List<Stmt> code) {
        for (ASTOptimization optimization : optimizations) {
            optimization.optimizeBlock(code);
        }
    }
}
