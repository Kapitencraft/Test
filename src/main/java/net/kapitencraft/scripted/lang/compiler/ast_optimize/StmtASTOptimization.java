package net.kapitencraft.scripted.lang.compiler.ast_optimize;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import net.kapitencraft.scripted.lang.holder.ast.ElifBranch;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.ast.SwitchKey;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;

import java.util.List;

public class StmtASTOptimization implements Stmt.Visitor<Stmt>, ASTOptimization {

    protected Expr.Literal createNumber(int value) {
        Expr.Literal literal = new Expr.Literal();
        literal.literal = new Token(TokenType.NUM, String.valueOf(value), new LiteralHolder(value, VarTypeManager.INTEGER), -1, -1);
        return literal;
    }

    public Stmt optimizeBlock(List<Stmt> statements) {
        statements.replaceAll(this::optimize);
        if (statements.size() == 1) {
            return statements.getFirst();
        }
        Stmt.Block block = new Stmt.Block();
        block.statements = statements;
        return block;
    }

    protected Stmt optimize(Stmt stmt) {
        return stmt.accept(this);
    }

    @Override
    public Stmt visitReturnStmt(Stmt.Return stmt) {
        return stmt;
    }

    @Override
    public Stmt visitForStmt(Stmt.For stmt) {
        stmt.init = optimize(stmt.init);
        stmt.body = optimize(stmt.body);
        return stmt;
    }

    @Override
    public Stmt visitWhileStmt(Stmt.While stmt) {
        stmt.body = optimize(stmt.body);
        return stmt;
    }

    @Override
    public Stmt visitForEachStmt(Stmt.ForEach stmt) {
        stmt.body = optimize(stmt.body);
        return stmt;
    }

    @Override
    public Stmt visitDebugTraceStmt(Stmt.DebugTrace stmt) {
        return stmt;
    }

    @Override
    public Stmt visitExpressionStmt(Stmt.Expression stmt) {
        return stmt;
    }

    @Override
    public Stmt visitVarDeclStmt(Stmt.VarDecl stmt) {
        return stmt;
    }

    @Override
    public Stmt visitThrowStmt(Stmt.Throw stmt) {
        return stmt;
    }

    @Override
    public Stmt visitBlockStmt(Stmt.Block stmt) {
        return optimizeBlock(stmt.statements);
    }


    @Override
    public Stmt visitTryStmt(Stmt.Try stmt) {
        //stmt.body = optimize(stmt.body);
        return stmt;
    }

    @Override
    public Stmt visitClearLocalsStmt(Stmt.ClearLocals stmt) {
        return stmt;
    }

    @Override
    public Stmt visitIfStmt(Stmt.If stmt) {
        if (stmt.thenBranch != null) {
            stmt.thenBranch = optimize(stmt.thenBranch);
        }
        for (ElifBranch elif : stmt.elifs) {
            elif.body = optimize(elif.body);
        }
        if (stmt.elseBranch != null) {
            stmt.elseBranch = optimize(stmt.elseBranch);
        }
        return stmt;
    }

    @Override
    public Stmt visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        return stmt;
    }
}
