package net.kapitencraft.scripted.lang.compiler.ast_optimize.impl;

import net.kapitencraft.scripted.lang.compiler.ast_optimize.ExprASTOptimization;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;

import java.util.List;

//TODO
public class VarMutationMergeOptimization extends ExprASTOptimization {
    @Override
    public Stmt optimizeBlock(List<Stmt> code) {
        for (int i = 0; i < code.size(); i++) {
            Stmt stmt = code.get(i);
        }
        return super.optimizeBlock(code);
    }

    //i += 5
    //i += 10
    //i += 8
    // ->
    //i += 5 + 10 + 8
}
