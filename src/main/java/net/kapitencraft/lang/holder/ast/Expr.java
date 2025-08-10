package net.kapitencraft.lang.holder.ast;

import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.algebra.Operand;
import java.util.Map;
import net.kapitencraft.lang.holder.LiteralHolder;

public interface Expr {

    interface Visitor<R> {
        R visitVarRefExpr(VarRef expr);
        R visitSetExpr(Set expr);
        R visitArraySetExpr(ArraySet expr);
        R visitArraySpecialExpr(ArraySpecial expr);
        R visitSpecialAssignExpr(SpecialAssign expr);
        R visitConstructorExpr(Constructor expr);
        R visitInstCallExpr(InstCall expr);
        R visitStaticSetExpr(StaticSet expr);
        R visitLogicalExpr(Logical expr);
        R visitGroupingExpr(Grouping expr);
        R visitUnaryExpr(Unary expr);
        R visitWhenExpr(When expr);
        R visitCastCheckExpr(CastCheck expr);
        R visitStaticGetExpr(StaticGet expr);
        R visitSwitchExpr(Switch expr);
        R visitSliceExpr(Slice expr);
        R visitGetExpr(Get expr);
        R visitArrayGetExpr(ArrayGet expr);
        R visitLiteralExpr(Literal expr);
        R visitAssignExpr(Assign expr);
        R visitStaticCallExpr(StaticCall expr);
        R visitBinaryExpr(Binary expr);
        R visitStaticSpecialExpr(StaticSpecial expr);
        R visitSpecialSetExpr(SpecialSet expr);
    }

    <R> R accept(Visitor<R> visitor);

    record VarRef(
        Token name, 
        byte ordinal
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarRefExpr(this);
        }
    }

    record Set(
        CompileExpr object, 
        Token name, 
        CompileExpr value, 
        Token assignType, 
        ClassReference executor, 
        Operand operand
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    record ArraySet(
        CompileExpr object, 
        CompileExpr index, 
        CompileExpr value, 
        Token assignType, 
        ClassReference executor, 
        Operand operand
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySetExpr(this);
        }
    }

    record ArraySpecial(
        CompileExpr object, 
        CompileExpr index, 
        Token assignType, 
        ClassReference executor
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySpecialExpr(this);
        }
    }

    record SpecialAssign(
        Token name, 
        Token assignType
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialAssignExpr(this);
        }
    }

    record Constructor(
        Token keyword, 
        ClassReference target, 
        CompileExpr[] params, 
        int ordinal
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitConstructorExpr(this);
        }
    }

    record InstCall(
        CompileExpr callee, 
        Token name, 
        CompileExpr[] args, 
        ClassReference retType, 
        String id
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitInstCallExpr(this);
        }
    }

    record StaticSet(
        ClassReference target, 
        Token name, 
        CompileExpr value, 
        Token assignType, 
        ClassReference executor, 
        Operand operand
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSetExpr(this);
        }
    }

    record Logical(
        CompileExpr left, 
        Token operator, 
        CompileExpr right
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    record Grouping(
        CompileExpr expression
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    record Unary(
        Token operator, 
        CompileExpr right, 
        ClassReference executor
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    record When(
        CompileExpr condition, 
        CompileExpr ifTrue, 
        CompileExpr ifFalse
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhenExpr(this);
        }
    }

    record CastCheck(
        CompileExpr object, 
        ClassReference targetType, 
        Token patternVarName
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCastCheckExpr(this);
        }
    }

    record StaticGet(
        ClassReference target, 
        Token name
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticGetExpr(this);
        }
    }

    record Switch(
        CompileExpr provider, 
        Map<Object,CompileExpr> params, 
        CompileExpr defaulted, 
        Token keyword
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchExpr(this);
        }
    }

    record Slice(
        CompileExpr object, 
        CompileExpr start, 
        CompileExpr end, 
        CompileExpr interval
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSliceExpr(this);
        }
    }

    record Get(
        CompileExpr object, 
        Token name
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    record ArrayGet(
        CompileExpr object, 
        CompileExpr index
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayGetExpr(this);
        }
    }

    record Literal(
        Token literal
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    record Assign(
        Token name, 
        CompileExpr value, 
        Token type, 
        byte ordinal, 
        ClassReference executor, 
        Operand operand
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    record StaticCall(
        ClassReference target, 
        Token name, 
        CompileExpr[] args, 
        ClassReference retType, 
        String id
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticCallExpr(this);
        }
    }

    record Binary(
        CompileExpr left, 
        CompileExpr right, 
        Token operator, 
        ClassReference executor, 
        Operand operand
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    record StaticSpecial(
        ClassReference target, 
        Token name, 
        Token assignType, 
        ClassReference executor
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSpecialExpr(this);
        }
    }

    record SpecialSet(
        CompileExpr callee, 
        Token name, 
        Token assignType, 
        ClassReference retType
    ) implements Expr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialSetExpr(this);
        }
    }
}
