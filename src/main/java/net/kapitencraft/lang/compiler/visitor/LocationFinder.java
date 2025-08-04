package net.kapitencraft.lang.compiler.visitor;

import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.holder.ast.CompileStmt;
import net.kapitencraft.lang.holder.token.Token;

public class LocationFinder implements CompileStmt.Visitor<Token>, CompileExpr.Visitor<Token> {

    public Token find(CompileStmt stmt) {
        return stmt.accept(this);
    }

    public Token find(CompileExpr expr) {
        return expr.accept(this);
    }

    @Override
    public Token visitAssignExpr(CompileExpr.Assign expr) {
        return expr.name();
    }

    @Override
    public Token visitSpecialAssignExpr(CompileExpr.SpecialAssign expr) {
        return expr.name();
    }

    @Override
    public Token visitBinaryExpr(CompileExpr.Binary expr) {
        return find(expr.left());
    }

    @Override
    public Token visitWhenExpr(CompileExpr.When expr) {
        return find(expr.condition());
    }

    @Override
    public Token visitInstCallExpr(CompileExpr.InstCall expr) {
        return expr.name();
    }

    @Override
    public Token visitStaticCallExpr(CompileExpr.StaticCall expr) {
        return expr.name();
    }

    @Override
    public Token visitGetExpr(CompileExpr.Get expr) {
        return expr.name();
    }

    @Override
    public Token visitStaticGetExpr(CompileExpr.StaticGet expr) {
        return expr.name();
    }

    @Override
    public Token visitArrayGetExpr(CompileExpr.ArrayGet expr) {
        return find(expr.object());
    }

    @Override
    public Token visitSetExpr(CompileExpr.Set expr) {
        return expr.name();
    }

    @Override
    public Token visitStaticSetExpr(CompileExpr.StaticSet expr) {
        return expr.name();
    }

    @Override
    public Token visitArraySetExpr(CompileExpr.ArraySet expr) {
        return expr.assignType();
    }

    @Override
    public Token visitSpecialSetExpr(CompileExpr.SpecialSet expr) {
        return expr.name();
    }

    @Override
    public Token visitStaticSpecialExpr(CompileExpr.StaticSpecial expr) {
        return expr.name();
    }

    @Override
    public Token visitArraySpecialExpr(CompileExpr.ArraySpecial expr) {
        return expr.assignType();
    }

    @Override
    public Token visitSliceExpr(CompileExpr.Slice expr) {
        return find(expr.object());
    }

    @Override
    public Token visitSwitchExpr(CompileExpr.Switch expr) {
        return expr.keyword();
    }

    @Override
    public Token visitCastCheckExpr(CompileExpr.CastCheck expr) {
        return find(expr.object());
    }

    @Override
    public Token visitGroupingExpr(CompileExpr.Grouping expr) {
        return find(expr.expression());
    }

    @Override
    public Token visitLiteralExpr(CompileExpr.Literal expr) {
        return expr.literal();
    }

    @Override
    public Token visitLogicalExpr(CompileExpr.Logical expr) {
        return expr.operator();
    }

    @Override
    public Token visitUnaryExpr(CompileExpr.Unary expr) {
        return expr.operator();
    }

    @Override
    public Token visitVarRefExpr(CompileExpr.VarRef expr) {
        return expr.name();
    }

    @Override
    public Token visitConstructorExpr(CompileExpr.Constructor expr) {
        return expr.keyword();
    }

    @Override
    public Token visitBlockStmt(CompileStmt.Block stmt) {
        return null; //don't return anything
    }

    @Override
    public Token visitExpressionStmt(CompileStmt.Expression stmt) {
        return find(stmt.expression());
    }

    @Override
    public Token visitIfStmt(CompileStmt.If stmt) {
        return stmt.keyword();
    }

    @Override
    public Token visitReturnStmt(CompileStmt.Return stmt) {
        return stmt.keyword();
    }

    @Override
    public Token visitThrowStmt(CompileStmt.Throw stmt) {
        return stmt.keyword();
    }

    @Override
    public Token visitVarDeclStmt(CompileStmt.VarDecl stmt) {
        return stmt.name();
    }

    @Override
    public Token visitWhileStmt(CompileStmt.While stmt) {
        return stmt.keyword();
    }

    @Override
    public Token visitForStmt(CompileStmt.For stmt) {
        return stmt.keyword();
    }

    @Override
    public Token visitForEachStmt(CompileStmt.ForEach stmt) {
        return stmt.name();
    }

    @Override
    public Token visitLoopInterruptionStmt(CompileStmt.LoopInterruption stmt) {
        return stmt.type();
    }

    @Override
    public Token visitTryStmt(CompileStmt.Try stmt) {
        return null;
    }
}
