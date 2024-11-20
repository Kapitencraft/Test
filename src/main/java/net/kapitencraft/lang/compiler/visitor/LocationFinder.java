package net.kapitencraft.lang.compiler.visitor;

import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.lang.holder.token.Token;

public class LocationFinder implements Stmt.Visitor<Token>, Expr.Visitor<Token> {

    public Token find(Stmt stmt) {
        return stmt.accept(this);
    }

    public Token find(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Token visitAssignExpr(Expr.Assign expr) {
        return expr.name;
    }

    @Override
    public Token visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        return expr.name;
    }

    @Override
    public Token visitBinaryExpr(Expr.Binary expr) {
        return find(expr.left);
    }

    @Override
    public Token visitWhenExpr(Expr.When expr) {
        return find(expr.condition);
    }

    @Override
    public Token visitInstCallExpr(Expr.InstCall expr) {
        return expr.name;
    }

    @Override
    public Token visitStaticCallExpr(Expr.StaticCall expr) {
        return expr.name;
    }

    @Override
    public Token visitGetExpr(Expr.Get expr) {
        return expr.name;
    }

    @Override
    public Token visitStaticGetExpr(Expr.StaticGet expr) {
        return expr.name;
    }

    @Override
    public Token visitArrayGetExpr(Expr.ArrayGet expr) {
        return find(expr.object);
    }

    @Override
    public Token visitSetExpr(Expr.Set expr) {
        return expr.name;
    }

    @Override
    public Token visitStaticSetExpr(Expr.StaticSet expr) {
        return expr.name;
    }

    @Override
    public Token visitArraySetExpr(Expr.ArraySet expr) {
        return expr.assignType;
    }

    @Override
    public Token visitSpecialSetExpr(Expr.SpecialSet expr) {
        return expr.name;
    }

    @Override
    public Token visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        return expr.name;
    }

    @Override
    public Token visitArraySpecialExpr(Expr.ArraySpecial expr) {
        return expr.assignType;
    }

    @Override
    public Token visitSwitchExpr(Expr.Switch expr) {
        return expr.keyword;
    }

    @Override
    public Token visitCastCheckExpr(Expr.CastCheck expr) {
        return find(expr.object);
    }

    @Override
    public Token visitGroupingExpr(Expr.Grouping expr) {
        return find(expr.expression);
    }

    @Override
    public Token visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Token visitLogicalExpr(Expr.Logical expr) {
        return expr.operator;
    }

    @Override
    public Token visitUnaryExpr(Expr.Unary expr) {
        return expr.operator;
    }

    @Override
    public Token visitVarRefExpr(Expr.VarRef expr) {
        return expr.name;
    }

    @Override
    public Token visitConstructorExpr(Expr.Constructor expr) {
        return expr.keyword;
    }

    @Override
    public Token visitBlockStmt(Stmt.Block stmt) {
        return null; //don't return anything
    }

    @Override
    public Token visitExpressionStmt(Stmt.Expression stmt) {
        return find(stmt.expression);
    }

    @Override
    public Token visitIfStmt(Stmt.If stmt) {
        return stmt.keyword;
    }

    @Override
    public Token visitReturnStmt(Stmt.Return stmt) {
        return stmt.keyword;
    }

    @Override
    public Token visitThrowStmt(Stmt.Throw stmt) {
        return stmt.keyword;
    }

    @Override
    public Token visitVarDeclStmt(Stmt.VarDecl stmt) {
        return stmt.name;
    }

    @Override
    public Token visitWhileStmt(Stmt.While stmt) {
        return stmt.keyword;
    }

    @Override
    public Token visitForStmt(Stmt.For stmt) {
        return stmt.keyword;
    }

    @Override
    public Token visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        return stmt.type;
    }

    @Override
    public Token visitTryStmt(Stmt.Try stmt) {
        return null;
    }
}
