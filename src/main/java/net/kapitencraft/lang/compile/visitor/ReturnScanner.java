package net.kapitencraft.lang.compile.visitor;

import net.kapitencraft.lang.compile.Compiler;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.tool.Pair;

public class ReturnScanner implements Stmt.Visitor<Boolean> {
    private final Compiler.ErrorLogger errorLogger;

    public ReturnScanner(Compiler.ErrorLogger errorLogger) {
        this.errorLogger = errorLogger;
    }

    public boolean scanReturn(Stmt stmt) {
        return stmt.accept(this);
    }

    @Override
    public Boolean visitImportStmt(Stmt.Import stmt) {
        return false;
    }

    @Override
    public Boolean visitBlockStmt(Stmt.Block stmt) {
        boolean seenReturn = false;
        for (int i = 0; i < stmt.statements.size(); i++) {
            Stmt stmt1 = stmt.statements.get(i);
            if (seenReturn) errorLogger.error(stmt1, "unreachable statement");
            if (scanReturn(stmt1)) {
                seenReturn = true;
            }
        }
        return seenReturn;
    }

    @Override
    public Boolean visitClassStmt(Stmt.Class stmt) {
        return false;
    }

    @Override
    public Boolean visitExpressionStmt(Stmt.Expression stmt) {
        return false;
    }

    @Override
    public Boolean visitFuncDeclStmt(Stmt.FuncDecl stmt) {
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
    public Boolean visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        return false;
    }
}
