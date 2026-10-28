package net.kapitencraft.scripted.lang.compiler.ast_optimize.impl;

import net.kapitencraft.scripted.lang.compiler.ast_optimize.StmtASTOptimization;
import net.kapitencraft.scripted.lang.holder.ast.ElifBranch;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;

import java.util.ArrayList;
import java.util.List;

public class ConstantConditionIfOptimization extends StmtASTOptimization {

    @Override
    public Stmt visitIfStmt(Stmt.If stmt) {
        if (stmt.condition instanceof Expr.Literal literal) {
            if (((boolean) literal.literal.literal().value())) {
                return optimize(stmt.thenBranch);
            }
            if (stmt.elifs.length > 0) {
                List<ElifBranch> remaining = new ArrayList<>();
                for (int i = 0; i < stmt.elifs.length; i++) {
                    ElifBranch branch = stmt.elifs[i];
                    if (branch.condition instanceof Expr.Literal branchLiteral) {
                        if (!((boolean) branchLiteral.literal.literal().value())) {
                            continue;
                        }
                        if (remaining.isEmpty())
                            return optimize(branch.body);
                    }
                    remaining.add(branch);
                }
                if (!remaining.isEmpty()) {
                    Stmt.If newIf = new Stmt.If();
                    ElifBranch first = remaining.getFirst();
                    newIf.condition = first.condition;
                    newIf.thenBranch = optimize(first.body);
                    newIf.branchSeenReturn = first.ended;
                    remaining.removeFirst();
                    newIf.elifs = remaining.toArray(new ElifBranch[0]);
                    newIf.elseBranch = optimize(stmt.elseBranch);
                    newIf.elseBranchSeenReturn = stmt.elseBranchSeenReturn;
                    newIf.keyword = stmt.keyword;
                    return newIf;
                }
            }
            return optimize(stmt.elseBranch);
        }
        return super.visitIfStmt(stmt);
    }
}
