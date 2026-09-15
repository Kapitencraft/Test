package net.kapitencraft.lang.compiler.analyser;

import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;

public class ReturnTypeAnalyser implements Expr.Visitor<ClassReference> {
    private final LocalVariableContainer localVariableContainer = new LocalVariableContainer();

    @Override
    public ClassReference visitSetExpr(Expr.Set expr) {
        return null;
    }

    @Override
    public ClassReference visitArraySpecialExpr(Expr.ArraySpecial expr) {
        return null;
    }

    @Override
    public ClassReference visitLogicalExpr(Expr.Logical expr) {
        return null;
    }

    @Override
    public ClassReference visitIdentifierSpecialAssignExpr(Expr.IdentifierSpecialAssign expr) {
        return null;
    }

    @Override
    public ClassReference visitComparisonChainExpr(Expr.ComparisonChain expr) {
        return null;
    }

    @Override
    public ClassReference visitCastCheckExpr(Expr.CastCheck expr) {
        return null;
    }

    @Override
    public ClassReference visitMethodRefExpr(Expr.MethodRef expr) {
        return null;
    }

    @Override
    public ClassReference visitIdentifierAssignExpr(Expr.IdentifierAssign expr) {
        return null;
    }

    @Override
    public ClassReference visitArrayGetExpr(Expr.ArrayGet expr) {
        return null;
    }

    @Override
    public ClassReference visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public ClassReference visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
        return null;
    }

    @Override
    public ClassReference visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        return null;
    }

    @Override
    public ClassReference visitSpecialSetExpr(Expr.SpecialSet expr) {
        return null;
    }

    @Override
    public ClassReference visitExprLambdaExpr(Expr.ExprLambda expr) {
        return null;
    }

    @Override
    public ClassReference visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public ClassReference visitArraySetExpr(Expr.ArraySet expr) {
        return null;
    }

    @Override
    public ClassReference visitSingleIdentifierExpr(Expr.SingleIdentifier expr) {
        return null;
    }

    @Override
    public ClassReference visitConstructorExpr(Expr.Constructor expr) {
        return null;
    }

    @Override
    public ClassReference visitStaticSetExpr(Expr.StaticSet expr) {
        return null;
    }

    @Override
    public ClassReference visitUnaryExpr(Expr.Unary expr) {
        return null;
    }

    @Override
    public ClassReference visitWhenExpr(Expr.When expr) {
        return null;
    }

    @Override
    public ClassReference visitBlockLambdaExpr(Expr.BlockLambda expr) {
        return null;
    }

    @Override
    public ClassReference visitStaticGetExpr(Expr.StaticGet expr) {
        return null;
    }

    @Override
    public ClassReference visitSwitchExpr(Expr.Switch expr) {
        return null;
    }

    @Override
    public ClassReference visitSliceExpr(Expr.Slice expr) {
        return null;
    }

    @Override
    public ClassReference visitGetExpr(Expr.Get expr) {
        return null;
    }

    @Override
    public ClassReference visitStaticMethodRefExpr(Expr.StaticMethodRef expr) {
        return null;
    }

    @Override
    public ClassReference visitBinaryExpr(Expr.Binary expr) {
        return null;
    }
}
