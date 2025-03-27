package net.kapitencraft.lang.holder.ast;

import net.kapitencraft.lang.holder.token.RuntimeToken;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.run.algebra.Operand;
import java.util.Map;
import net.kapitencraft.lang.holder.LiteralHolder;

public abstract class RuntimeExpr {

    public interface Visitor<R> {
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

    public static class VarRef extends RuntimeExpr {
        public final RuntimeToken name;

        public VarRef(RuntimeToken name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarRefExpr(this);
        }
    }

    public static class Set extends RuntimeExpr {
        public final RuntimeExpr object;
        public final String name;
        public final RuntimeExpr value;
        public final TokenType assignType;
        public final int line;
        public final ClassReference executor;
        public final Operand operand;

        public Set(RuntimeExpr object, String name, RuntimeExpr value, TokenType assignType, int line, ClassReference executor, Operand operand) {
            this.object = object;
            this.name = name;
            this.value = value;
            this.assignType = assignType;
            this.line = line;
            this.executor = executor;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    public static class ArraySet extends RuntimeExpr {
        public final RuntimeExpr object;
        public final RuntimeExpr index;
        public final RuntimeExpr value;
        public final TokenType assignType;
        public final int line;
        public final ClassReference executor;
        public final Operand operand;

        public ArraySet(RuntimeExpr object, RuntimeExpr index, RuntimeExpr value, TokenType assignType, int line, ClassReference executor, Operand operand) {
            this.object = object;
            this.index = index;
            this.value = value;
            this.assignType = assignType;
            this.line = line;
            this.executor = executor;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySetExpr(this);
        }
    }

    public static class ArraySpecial extends RuntimeExpr {
        public final RuntimeExpr object;
        public final RuntimeExpr index;
        public final TokenType assignType;

        public ArraySpecial(RuntimeExpr object, RuntimeExpr index, TokenType assignType) {
            this.object = object;
            this.index = index;
            this.assignType = assignType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySpecialExpr(this);
        }
    }

    public static class SpecialAssign extends RuntimeExpr {
        public final RuntimeToken name;
        public final TokenType assignType;

        public SpecialAssign(RuntimeToken name, TokenType assignType) {
            this.name = name;
            this.assignType = assignType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialAssignExpr(this);
        }
    }

    public static class Constructor extends RuntimeExpr {
        public final RuntimeToken keyword;
        public final ClassReference target;
        public final RuntimeExpr[] params;
        public final int ordinal;

        public Constructor(RuntimeToken keyword, ClassReference target, RuntimeExpr[] params, int ordinal) {
            this.keyword = keyword;
            this.target = target;
            this.params = params;
            this.ordinal = ordinal;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitConstructorExpr(this);
        }
    }

    public static class InstCall extends RuntimeExpr {
        public final RuntimeExpr callee;
        public final RuntimeToken name;
        public final int methodOrdinal;
        public final RuntimeExpr[] args;

        public InstCall(RuntimeExpr callee, RuntimeToken name, int methodOrdinal, RuntimeExpr[] args) {
            this.callee = callee;
            this.name = name;
            this.methodOrdinal = methodOrdinal;
            this.args = args;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitInstCallExpr(this);
        }
    }

    public static class StaticSet extends RuntimeExpr {
        public final ClassReference target;
        public final String name;
        public final RuntimeExpr value;
        public final TokenType assignType;
        public final int line;
        public final ClassReference executor;
        public final Operand operand;

        public StaticSet(ClassReference target, String name, RuntimeExpr value, TokenType assignType, int line, ClassReference executor, Operand operand) {
            this.target = target;
            this.name = name;
            this.value = value;
            this.assignType = assignType;
            this.line = line;
            this.executor = executor;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSetExpr(this);
        }
    }

    public static class Logical extends RuntimeExpr {
        public final RuntimeExpr left;
        public final TokenType operator;
        public final RuntimeExpr right;

        public Logical(RuntimeExpr left, TokenType operator, RuntimeExpr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    public static class Grouping extends RuntimeExpr {
        public final RuntimeExpr expression;

        public Grouping(RuntimeExpr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Unary extends RuntimeExpr {
        public final TokenType operator;
        public final RuntimeExpr right;

        public Unary(TokenType operator, RuntimeExpr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class When extends RuntimeExpr {
        public final RuntimeExpr condition;
        public final RuntimeExpr ifTrue;
        public final RuntimeExpr ifFalse;

        public When(RuntimeExpr condition, RuntimeExpr ifTrue, RuntimeExpr ifFalse) {
            this.condition = condition;
            this.ifTrue = ifTrue;
            this.ifFalse = ifFalse;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhenExpr(this);
        }
    }

    public static class CastCheck extends RuntimeExpr {
        public final RuntimeExpr object;
        public final ClassReference targetType;
        public final String patternVarName;

        public CastCheck(RuntimeExpr object, ClassReference targetType, String patternVarName) {
            this.object = object;
            this.targetType = targetType;
            this.patternVarName = patternVarName;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCastCheckExpr(this);
        }
    }

    public static class StaticGet extends RuntimeExpr {
        public final ClassReference target;
        public final String name;

        public StaticGet(ClassReference target, String name) {
            this.target = target;
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticGetExpr(this);
        }
    }

    public static class Switch extends RuntimeExpr {
        public final RuntimeExpr provider;
        public final Map<Object,RuntimeExpr> params;
        public final RuntimeExpr defaulted;
        public final RuntimeToken keyword;

        public Switch(RuntimeExpr provider, Map<Object,RuntimeExpr> params, RuntimeExpr defaulted, RuntimeToken keyword) {
            this.provider = provider;
            this.params = params;
            this.defaulted = defaulted;
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchExpr(this);
        }
    }

    public static class Slice extends RuntimeExpr {
        public final RuntimeExpr object;
        public final RuntimeExpr start;
        public final RuntimeExpr end;
        public final RuntimeExpr interval;

        public Slice(RuntimeExpr object, RuntimeExpr start, RuntimeExpr end, RuntimeExpr interval) {
            this.object = object;
            this.start = start;
            this.end = end;
            this.interval = interval;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSliceExpr(this);
        }
    }

    public static class Get extends RuntimeExpr {
        public final RuntimeExpr object;
        public final String name;

        public Get(RuntimeExpr object, String name) {
            this.object = object;
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    public static class ArrayGet extends RuntimeExpr {
        public final RuntimeExpr object;
        public final RuntimeExpr index;

        public ArrayGet(RuntimeExpr object, RuntimeExpr index) {
            this.object = object;
            this.index = index;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayGetExpr(this);
        }
    }

    public static class Literal extends RuntimeExpr {
        public final LiteralHolder literal;

        public Literal(LiteralHolder literal) {
            this.literal = literal;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Assign extends RuntimeExpr {
        public final RuntimeToken name;
        public final RuntimeExpr value;
        public final TokenType type;
        public final ClassReference executor;
        public final int line;
        public final Operand operand;

        public Assign(RuntimeToken name, RuntimeExpr value, TokenType type, ClassReference executor, int line, Operand operand) {
            this.name = name;
            this.value = value;
            this.type = type;
            this.executor = executor;
            this.line = line;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class StaticCall extends RuntimeExpr {
        public final ClassReference target;
        public final RuntimeToken name;
        public final int methodOrdinal;
        public final RuntimeExpr[] args;

        public StaticCall(ClassReference target, RuntimeToken name, int methodOrdinal, RuntimeExpr[] args) {
            this.target = target;
            this.name = name;
            this.methodOrdinal = methodOrdinal;
            this.args = args;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticCallExpr(this);
        }
    }

    public static class Binary extends RuntimeExpr {
        public final RuntimeExpr left;
        public final RuntimeExpr right;
        public final TokenType operator;
        public final ClassReference executor;
        public final int line;
        public final Operand operand;

        public Binary(RuntimeExpr left, RuntimeExpr right, TokenType operator, ClassReference executor, int line, Operand operand) {
            this.left = left;
            this.right = right;
            this.operator = operator;
            this.executor = executor;
            this.line = line;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class StaticSpecial extends RuntimeExpr {
        public final ClassReference target;
        public final String name;
        public final TokenType assignType;

        public StaticSpecial(ClassReference target, String name, TokenType assignType) {
            this.target = target;
            this.name = name;
            this.assignType = assignType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSpecialExpr(this);
        }
    }

    public static class SpecialSet extends RuntimeExpr {
        public final RuntimeExpr callee;
        public final String name;
        public final TokenType assignType;

        public SpecialSet(RuntimeExpr callee, String name, TokenType assignType) {
            this.callee = callee;
            this.name = name;
            this.assignType = assignType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialSetExpr(this);
        }
    }

  public abstract <R> R accept(Visitor<R> visitor);
}
