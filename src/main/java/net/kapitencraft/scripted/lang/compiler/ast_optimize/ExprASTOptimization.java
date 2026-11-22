package net.kapitencraft.scripted.lang.compiler.ast_optimize;

import net.kapitencraft.scripted.lang.exe.VarTypeManager;
import net.kapitencraft.scripted.lang.holder.LiteralHolder;
import net.kapitencraft.scripted.lang.holder.ast.ElifBranch;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.scripted.lang.holder.ast.SwitchKey;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.kapitencraft.scripted.lang.holder.token.TokenType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExprASTOptimization implements Expr.Visitor<@NotNull Expr>, Stmt.Visitor<@NotNull Stmt>, ASTOptimization {

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

    protected Expr optimize(Expr expr) {
        return expr.accept(this);
    }

    private Expr optimizeIfNonNull(Expr object) {
        if (object != null)
            return optimize(object);
        return null;
    }

    private void optimizeArray(Expr[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = optimize(array[i]);
        }
    }

    protected Stmt optimize(Stmt stmt) {
        return stmt.accept(this);
    }

    private Stmt optimizeIfNonNull(Stmt object) {
        if (object != null)
            return optimize(object);
        return null;
    }

    @Override
    public Expr visitCallExpr(Expr.Call expr) {
        if (expr.virtual)
            expr.object = optimize(expr.object);
        optimizeArray(expr.args);
        return expr;
    }

    @Override
    public Expr visitSetExpr(Expr.Set expr) {
        expr.object = optimize(expr.object);
        expr.value = optimize(expr.value);
        return expr;
    }

    @Override
    public Expr visitArraySetExpr(Expr.ArraySet expr) {
        expr.index = optimize(expr.index);
        expr.object = optimize(expr.object);
        expr.value = optimize(expr.value);
        return expr;
    }

    @Override
    public Expr visitArraySpecialExpr(Expr.ArraySpecial expr) {
        expr.index = optimize(expr.index);
        expr.object = optimize(expr.object);
        return expr;
    }

    @Override
    public Expr visitSingleIdentifierExpr(Expr.SingleIdentifier expr) {
        return expr;
    }

    @Override
    public Expr visitConstructorExpr(Expr.Constructor expr) {
        optimizeArray(expr.args);
        return expr;

    }

    @Override
    public Expr visitStaticSetExpr(Expr.StaticSet expr) {
        expr.value = optimize(expr.value);
        return expr;

    }

    @Override
    public Expr visitLogicalExpr(Expr.Logical expr) {
        expr.left = optimize(expr.left);
        expr.right = optimize(expr.right);
        return expr;

    }

    @Override
    public Expr visitIdentifierSpecialAssignExpr(Expr.IdentifierSpecialAssign expr) {
        return expr;
    }

    @Override
    public Expr visitUnaryExpr(Expr.Unary expr) {
        expr.right = optimize(expr.right);
        return expr;
    }

    @Override
    public Expr visitComparisonChainExpr(Expr.ComparisonChain expr) {
        optimizeArray(expr.entries);
        return expr;
    }

    @Override
    public Expr visitWhenExpr(Expr.When expr) {
        expr.condition = optimize(expr.condition);
        expr.ifTrue = optimize(expr.ifTrue);
        expr.ifFalse = optimize(expr.ifFalse);
        return expr;
    }

    @Override
    public Expr visitBlockLambdaExpr(Expr.BlockLambda expr) {
        return expr;
    }

    @Override
    public Expr visitCastCheckExpr(Expr.CastCheck expr) {
        expr.object = optimize(expr.object);
        return expr;
    }

    @Override
    public Expr visitMethodRefExpr(Expr.MethodRef expr) {
        expr.obj = optimize(expr.obj);
        return expr;
    }

    @Override
    public Expr visitStaticGetExpr(Expr.StaticGet expr) {
        return expr;
    }

    @Override
    public Expr visitSwitchExpr(Expr.Switch expr) {
        expr.provider = optimize(expr.provider);
        for (int i = 0; i < expr.params.length; i++) {
            SwitchKey param = expr.params[i];
            param.expr = optimize(param.expr);
        }
        expr.defaulted = optimizeIfNonNull(expr.defaulted);
        return expr;
    }

    @Override
    public Expr visitIdentifierAssignExpr(Expr.IdentifierAssign expr) {
        expr.value = optimize(expr.value);
        return expr;
    }

    @Override
    public Expr visitSliceExpr(Expr.Slice expr) {
        expr.object = optimize(expr.object);
        expr.start = optimizeIfNonNull(expr.start);
        expr.end = optimizeIfNonNull(expr.end);
        expr.interval = optimizeIfNonNull(expr.interval);
        return expr;
    }

    @Override
    public Expr visitGetExpr(Expr.Get expr) {
        expr.object = optimize(expr.object);
        return expr;
    }

    @Override
    public Expr visitStaticMethodRefExpr(Expr.StaticMethodRef expr) {
        return expr;
    }

    @Override
    public Expr visitArrayGetExpr(Expr.ArrayGet expr) {
        expr.object = optimize(expr.object);
        expr.index = optimize(expr.index);
        return expr;
    }

    @Override
    public Expr visitLiteralExpr(Expr.Literal expr) {
        return expr;
    }

    @Override
    public Expr visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
        if (expr.size != null) {
            expr.size = optimize(expr.size);
        } else {
            optimizeArray(expr.obj);
        }
        return expr;
    }

    @Override
    public Expr visitBinaryExpr(Expr.Binary expr) {
        expr.left = optimize(expr.left);
        expr.right = optimize(expr.right);
        return expr;
    }

    @Override
    public Expr visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        return expr;
    }

    @Override
    public Expr visitSpecialSetExpr(Expr.SpecialSet expr) {
        expr.object = optimize(expr.object);
        return expr;
    }

    @Override
    public Expr visitExprLambdaExpr(Expr.ExprLambda expr) {
        optimizeArray(expr.captures);
        return expr;
    }

    @Override
    public Stmt visitReturnStmt(Stmt.Return stmt) {
        stmt.value = optimizeIfNonNull(stmt.value);
        return stmt;
    }

    @Override
    public Stmt visitForStmt(Stmt.For stmt) {
        stmt.init = optimize(stmt.init);
        stmt.condition = optimize(stmt.condition);
        stmt.increment = optimize(stmt.increment);
        stmt.body = optimize(stmt.body);
        return stmt;
    }

    @Override
    public Stmt visitWhileStmt(Stmt.While stmt) {
        stmt.condition = optimize(stmt.condition);
        stmt.body = optimize(stmt.body);
        return stmt;
    }

    @Override
    public Stmt visitForEachStmt(Stmt.ForEach stmt) {
        stmt.initializer = optimize(stmt.initializer);
        stmt.body = optimize(stmt.body);
        return stmt;
    }

    @Override
    public Stmt visitDebugTraceStmt(Stmt.DebugTrace stmt) {
        return stmt;
    }

    @Override
    public Stmt visitExpressionStmt(Stmt.Expression stmt) {
        stmt.expression = optimize(stmt.expression);
        return stmt;
    }

    @Override
    public Stmt visitVarDeclStmt(Stmt.VarDecl stmt) {
        stmt.initializer = optimizeIfNonNull(stmt.initializer);
        return stmt;
    }

    @Override
    public Stmt visitThrowStmt(Stmt.Throw stmt) {
        stmt.value = optimize(stmt.value);
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
        stmt.condition = optimize(stmt.condition);
        stmt.thenBranch = optimizeIfNonNull(stmt.thenBranch);
        for (ElifBranch elif : stmt.elifs) {
            elif.condition = optimize(elif.condition);
            elif.body = optimize(elif.body);
        }
        stmt.elseBranch = optimizeIfNonNull(stmt.elseBranch);
        return stmt;
    }

    @Override
    public Stmt visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        return stmt;
    }
}
