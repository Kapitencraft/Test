package net.kapitencraft.lang.holder.ast;

import net.kapitencraft.lang.holder.token.RuntimeToken;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.run.algebra.Operand;
import java.util.Map;
import net.kapitencraft.lang.holder.LiteralHolder;

public interface RuntimeExpr {

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
        byte ordinal
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarRefExpr(this);
        }
    }

    record Set(
        RuntimeExpr object, 
        String name, 
        RuntimeExpr value, 
        TokenType assignType, 
        ClassReference executor, 
        Operand operand
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    record ArraySet(
        RuntimeExpr object, 
        RuntimeExpr index, 
        RuntimeExpr value, 
        TokenType assignType, 
        ClassReference executor, 
        Operand operand
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySetExpr(this);
        }
    }

    record ArraySpecial(
        RuntimeExpr object, 
        RuntimeExpr index, 
        TokenType assignType
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySpecialExpr(this);
        }
    }

    record SpecialAssign(
        RuntimeToken name, 
        TokenType assignType
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialAssignExpr(this);
        }
    }

    record Constructor(
        int line, 
        ClassReference target, 
        RuntimeExpr[] params, 
        int ordinal
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitConstructorExpr(this);
        }
    }

    record InstCall(
        RuntimeExpr callee, 
        RuntimeToken name, 
        int methodOrdinal, 
        RuntimeExpr[] args, 
        String id
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitInstCallExpr(this);
        }
    }

    record StaticSet(
        ClassReference target, 
        String name, 
        RuntimeExpr value, 
        TokenType assignType, 
        ClassReference executor, 
        Operand operand
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSetExpr(this);
        }
    }

    record Logical(
        RuntimeExpr left, 
        TokenType operator, 
        RuntimeExpr right
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    record Grouping(
        RuntimeExpr expression
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    record Unary(
        TokenType operator, 
        RuntimeExpr right
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    record When(
        RuntimeExpr condition, 
        RuntimeExpr ifTrue, 
        RuntimeExpr ifFalse
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhenExpr(this);
        }
    }

    record CastCheck(
        RuntimeExpr object, 
        ClassReference targetType, 
        String patternVarName
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCastCheckExpr(this);
        }
    }

    record StaticGet(
        ClassReference target, 
        String name
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticGetExpr(this);
        }
    }

    record Switch(
        RuntimeExpr provider, 
        Map<Object,RuntimeExpr> params, 
        RuntimeExpr defaulted
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchExpr(this);
        }
    }

    record Slice(
        RuntimeExpr object, 
        RuntimeExpr start, 
        RuntimeExpr end, 
        RuntimeExpr interval
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSliceExpr(this);
        }
    }

    record Get(
        RuntimeExpr object, 
        String name
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    record ArrayGet(
        RuntimeExpr object, 
        RuntimeExpr index
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayGetExpr(this);
        }
    }

    record Literal(
        LiteralHolder literal
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    record Assign(
        RuntimeToken name, 
        RuntimeExpr value, 
        TokenType type, 
        byte ordinal, 
        ClassReference executor, 
        Operand operand
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    record StaticCall(
        ClassReference target, 
        RuntimeToken name, 
        int methodOrdinal, 
        RuntimeExpr[] args, 
        String id
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticCallExpr(this);
        }
    }

    record Binary(
        RuntimeExpr left, 
        RuntimeExpr right, 
        TokenType operator, 
        ClassReference executor, 
        Operand operand
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    record StaticSpecial(
        ClassReference target, 
        String name, 
        TokenType assignType
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSpecialExpr(this);
        }
    }

    record SpecialSet(
        RuntimeExpr callee, 
        String name, 
        TokenType assignType
    ) implements RuntimeExpr {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialSetExpr(this);
        }
    }
}
