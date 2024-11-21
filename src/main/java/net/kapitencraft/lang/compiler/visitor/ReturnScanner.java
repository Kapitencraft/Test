package net.kapitencraft.lang.compiler.visitor;

import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.tool.Pair;

import java.util.List;

//TODO reimplement
public class ReturnScanner implements Stmt.Visitor<Boolean> {
    private final Compiler.ErrorLogger errorLogger;

    public ReturnScanner(Compiler.ErrorLogger errorLogger) {
        this.errorLogger = errorLogger;
    }

    public boolean scanList(List<Stmt> stmts) {
        boolean seenReturn = false;
        for (Stmt stmt1 : stmts) {
            if (seenReturn) errorLogger.error(stmt1, "unreachable statement");
            if (!seenReturn && scanReturn(stmt1)) {
                seenReturn = true;
            }
        }
        return seenReturn;
    }

    public boolean scanReturn(Stmt stmt) {
        return stmt.accept(this);
    }

    @Override
    public Boolean visitBlockStmt(Stmt.Block stmt) {
        return scanList(stmt.statements);
    }

    @Override
    public Boolean visitExpressionStmt(Stmt.Expression stmt) {
        return false;
    }

    @Override
    public Boolean visitIfStmt(Stmt.If stmt) {
        boolean seenReturn = true;
        seenReturn &= scanReturn(stmt.thenBranch);
        seenReturn &= stmt.elifs.stream().map(Pair::right).allMatch(this::scanReturn);
        seenReturn &= scanReturn(stmt.elseBranch);

        return seenReturn;
    }

    @Override
    public Boolean visitReturnStmt(Stmt.Return stmt) {
        return true;
    }

    @Override
    public Boolean visitThrowStmt(Stmt.Throw stmt) {
        return true;
    }

    @Override
    public Boolean visitVarDeclStmt(Stmt.VarDecl stmt) {
        return false;
    }

    @Override
    public Boolean visitWhileStmt(Stmt.While stmt) {
        return scanReturn(stmt.body);
    }

    @Override
    public Boolean visitForStmt(Stmt.For stmt) {
        return scanReturn(stmt.body);
    }

    @Override
    public Boolean visitForEachStmt(Stmt.ForEach stmt) {
        return scanReturn(stmt.body);
    }

    @Override
    public Boolean visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        return false;
    }

    @Override
    public Boolean visitTryStmt(Stmt.Try stmt) {
        return visitBlockStmt(stmt.finale) &&
                stmt.catches.stream().map(Pair::right).allMatch(this::visitBlockStmt) &&
                visitBlockStmt(stmt.body);
    }
}
