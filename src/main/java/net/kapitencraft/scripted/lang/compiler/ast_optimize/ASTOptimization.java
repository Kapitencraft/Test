package net.kapitencraft.scripted.lang.compiler.ast_optimize;

import net.kapitencraft.scripted.lang.holder.ast.Stmt;

import java.util.List;

public interface ASTOptimization {

    Stmt optimizeBlock(List<Stmt> code);
}
