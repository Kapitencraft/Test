package net.kapitencraft.lang.compiler.visitor;

import net.kapitencraft.lang.compiler.analyser.BytecodeVars;
import net.kapitencraft.lang.holder.ast.CompileExpr;
import net.kapitencraft.lang.holder.ast.CompileStmt;
import net.kapitencraft.lang.holder.ast.RuntimeExpr;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.analyser.VarAnalyser;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;

public class RetTypeFinder implements CompileExpr.Visitor<ClassReference> {
    private final BytecodeVars varAnalyser;

    //STAGE: Skeleton

    public ClassReference findRetType(CompileExpr expr) {
        return expr.accept(this);
    }

    public RetTypeFinder(BytecodeVars varAnalyser) {
        this.varAnalyser = varAnalyser;
    }

    @Override
    public ClassReference visitAssignExpr(CompileExpr.Assign expr) {
        return varAnalyser.getType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitSpecialAssignExpr(CompileExpr.SpecialAssign expr) {
        return varAnalyser.getType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitBinaryExpr(CompileExpr.Binary expr) {
        return expr.executor.get().checkOperation(OperationType.of(expr.operator.type()), expr.operand, findRetType(expr.operand == Operand.LEFT ? expr.right : expr.left)).reference();
    }

    @Override
    public ClassReference visitWhenExpr(CompileExpr.When expr) {
        return findRetType(expr.ifTrue);
    }

    @Override
    public ClassReference visitInstCallExpr(CompileExpr.InstCall expr) {
        return expr.retType;
    }

    @Override
    public ClassReference visitStaticCallExpr(CompileExpr.StaticCall expr) {
        return expr.retType;
    }

    @Override
    public ClassReference visitGetExpr(CompileExpr.Get expr) {
        return findRetType(expr.object).get().getFieldType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitStaticGetExpr(CompileExpr.StaticGet expr) {
        return expr.target == null ? VarTypeManager.VOID.reference() : expr.target.get().getStaticFieldType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitArrayGetExpr(CompileExpr.ArrayGet expr) {
        return findRetType(expr.object).get().getComponentType().reference();
    }

    @Override
    public ClassReference visitSetExpr(CompileExpr.Set expr) {
        return findRetType(expr.object).get().getFieldType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitStaticSetExpr(CompileExpr.StaticSet expr) {
        return expr.target.get().getStaticFieldType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitArraySetExpr(CompileExpr.ArraySet expr) {
        return findRetType(expr.object).get().getComponentType().reference();
    }

    @Override
    public ClassReference visitSpecialSetExpr(CompileExpr.SpecialSet expr) {
        return findRetType(expr.callee).get().getFieldType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitStaticSpecialExpr(CompileExpr.StaticSpecial expr) {
        return expr.target.get().getStaticFieldType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitArraySpecialExpr(CompileExpr.ArraySpecial expr) {
        return null;
    }

    @Override
    public ClassReference visitSliceExpr(CompileExpr.Slice expr) {
        return findRetType(expr.object);
    }

    @Override
    public ClassReference visitSwitchExpr(CompileExpr.Switch expr) {
        return findRetType(expr.defaulted);
    }

    @Override
    public ClassReference visitCastCheckExpr(CompileExpr.CastCheck expr) {
        return VarTypeManager.BOOLEAN.reference();
    }

    @Override
    public ClassReference visitGroupingExpr(CompileExpr.Grouping expr) {
        return findRetType(expr.expression);
    }

    @Override
    public ClassReference visitLiteralExpr(CompileExpr.Literal expr) {
        return expr.literal.literal().type().reference();
    }

    @Override
    public ClassReference visitLogicalExpr(CompileExpr.Logical expr) {
        return VarTypeManager.BOOLEAN.reference();
    }

    @Override
    public ClassReference visitUnaryExpr(CompileExpr.Unary expr) {
        return findRetType(expr.right);
    }

    @Override
    public ClassReference visitVarRefExpr(CompileExpr.VarRef expr) {
        return varAnalyser.getType(expr.name.lexeme());
    }

    @Override
    public ClassReference visitConstructorExpr(CompileExpr.Constructor expr) {
        return expr.target;
    }
}
