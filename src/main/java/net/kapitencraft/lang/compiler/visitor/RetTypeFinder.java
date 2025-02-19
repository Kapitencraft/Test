package net.kapitencraft.lang.compiler.visitor;

import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.analyser.VarAnalyser;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;

public class RetTypeFinder implements Expr.Visitor<ClassReference> {
    private final VarAnalyser varAnalyser;

    //STAGE: Skeleton

    public ClassReference findRetType(Expr expr) {
        return expr.accept(this);
    }

    public RetTypeFinder(VarAnalyser varAnalyser) {
        this.varAnalyser = varAnalyser;
    }

    @Override
    public ClassReference visitAssignExpr(Expr.Assign expr) {
        return varAnalyser.getType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        return varAnalyser.getType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitBinaryExpr(Expr.Binary expr) {
        return expr.executor.get().checkOperation(OperationType.of(expr.operator), expr.operand, findRetType(expr.operand == Operand.LEFT ? expr.right : expr.left)).reference();
    }

    @Override
    public ClassReference visitWhenExpr(Expr.When expr) {
        return findRetType(expr.ifTrue);
    }

    @Override
    public ClassReference visitInstCallExpr(Expr.InstCall expr) {
        return findRetType(expr.callee).get().getMethodByOrdinal(expr.name.lexeme(), expr.methodOrdinal).type();
    }

    @Override
    public ClassReference visitStaticCallExpr(Expr.StaticCall expr) {
        if (expr.target == null) return VarTypeManager.VOID.reference();
        return expr.target.get().getStaticMethodByOrdinal(expr.name.lexeme(), expr.methodOrdinal).type();
    }

    @Override
    public ClassReference visitGetExpr(Expr.Get expr) {
        return findRetType(expr.object).get().getFieldType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitStaticGetExpr(Expr.StaticGet expr) {
        return expr.target.get().getStaticFieldType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitArrayGetExpr(Expr.ArrayGet expr) {
        return findRetType(expr.object).get().getComponentType().reference();
    }

    @Override
    public ClassReference visitSetExpr(Expr.Set expr) {
        return findRetType(expr.object).get().getFieldType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitStaticSetExpr(Expr.StaticSet expr) {
        return expr.target.get().getStaticFieldType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitArraySetExpr(Expr.ArraySet expr) {
        return findRetType(expr.object).get().getComponentType().reference();
    }

    @Override
    public ClassReference visitSpecialSetExpr(Expr.SpecialSet expr) {
        return findRetType(expr.callee).get().getFieldType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        return expr.target.get().getStaticFieldType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitArraySpecialExpr(Expr.ArraySpecial expr) {
        return null;
    }

    @Override
    public ClassReference visitSwitchExpr(Expr.Switch expr) {
        return findRetType(expr.defaulted);
    }

    @Override
    public ClassReference visitCastCheckExpr(Expr.CastCheck expr) {
        return VarTypeManager.BOOLEAN.reference();
    }

    @Override
    public ClassReference visitGroupingExpr(Expr.Grouping expr) {
        return findRetType(expr.expression);
    }

    @Override
    public ClassReference visitLiteralExpr(Expr.Literal expr) {
        return expr.holder.type().reference();
    }

    @Override
    public ClassReference visitLogicalExpr(Expr.Logical expr) {
        return VarTypeManager.BOOLEAN.reference();
    }

    @Override
    public ClassReference visitUnaryExpr(Expr.Unary expr) {
        return findRetType(expr.right);
    }

    @Override
    public ClassReference visitVarRefExpr(Expr.VarRef expr) {
        return varAnalyser.getType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitConstructorExpr(Expr.Constructor expr) {
        return expr.target;
    }
}
