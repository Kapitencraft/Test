package net.kapitencraft.lang.holder.ast;

import net.kapitencraft.lang.holder.token.RuntimeToken;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
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
        public final ClassReference executor;
        public final String name;
        public final TokenType assignType;
        public final int line;
        public final RuntimeExpr value;
        public final Operand operand;
        public final RuntimeExpr object;

        public Set(ClassReference executor, String name, TokenType assignType, int line, RuntimeExpr value, Operand operand, RuntimeExpr object) {
            this.executor = executor;
            this.name = name;
            this.assignType = assignType;
            this.line = line;
            this.value = value;
            this.operand = operand;
            this.object = object;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    public static class ArraySet extends RuntimeExpr {
        public final ClassReference executor;
        public final RuntimeExpr index;
        public final TokenType assignType;
        public final int line;
        public final RuntimeExpr value;
        public final Operand operand;
        public final RuntimeExpr object;

        public ArraySet(ClassReference executor, RuntimeExpr index, TokenType assignType, int line, RuntimeExpr value, Operand operand, RuntimeExpr object) {
            this.executor = executor;
            this.index = index;
            this.assignType = assignType;
            this.line = line;
            this.value = value;
            this.operand = operand;
            this.object = object;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySetExpr(this);
        }
    }

    public static class ArraySpecial extends RuntimeExpr {
        public final RuntimeExpr index;
        public final TokenType assignType;
        public final RuntimeExpr object;

        public ArraySpecial(RuntimeExpr index, TokenType assignType, RuntimeExpr object) {
            this.index = index;
            this.assignType = assignType;
            this.object = object;
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
        public final RuntimeExpr[] params;
        public final ClassReference target;
        public final int ordinal;

        public Constructor(RuntimeToken keyword, RuntimeExpr[] params, ClassReference target, int ordinal) {
            this.keyword = keyword;
            this.params = params;
            this.target = target;
            this.ordinal = ordinal;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitConstructorExpr(this);
        }
    }

    public static class InstCall extends RuntimeExpr {
        public final RuntimeExpr[] args;
        public final RuntimeExpr callee;
        public final RuntimeToken name;
        public final int methodOrdinal;

        public InstCall(RuntimeExpr[] args, RuntimeExpr callee, RuntimeToken name, int methodOrdinal) {
            this.args = args;
            this.callee = callee;
            this.name = name;
            this.methodOrdinal = methodOrdinal;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitInstCallExpr(this);
        }
    }

    public static class StaticSet extends RuntimeExpr {
        public final ClassReference executor;
        public final String name;
        public final TokenType assignType;
        public final int line;
        public final RuntimeExpr value;
        public final Operand operand;
        public final ClassReference target;

        public StaticSet(ClassReference executor, String name, TokenType assignType, int line, RuntimeExpr value, Operand operand, ClassReference target) {
            this.executor = executor;
            this.name = name;
            this.assignType = assignType;
            this.line = line;
            this.value = value;
            this.operand = operand;
            this.target = target;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSetExpr(this);
        }
    }

    public static class Logical extends RuntimeExpr {
        public final RuntimeExpr left;
        public final RuntimeExpr right;
        public final TokenType operator;

        public Logical(RuntimeExpr left, RuntimeExpr right, TokenType operator) {
            this.left = left;
            this.right = right;
            this.operator = operator;
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
        public final RuntimeExpr right;
        public final TokenType operator;

        public Unary(RuntimeExpr right, TokenType operator) {
            this.right = right;
            this.operator = operator;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class When extends RuntimeExpr {
        public final RuntimeExpr condition;
        public final RuntimeExpr ifFalse;
        public final RuntimeExpr ifTrue;

        public When(RuntimeExpr condition, RuntimeExpr ifFalse, RuntimeExpr ifTrue) {
            this.condition = condition;
            this.ifFalse = ifFalse;
            this.ifTrue = ifTrue;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhenExpr(this);
        }
    }

    public static class CastCheck extends RuntimeExpr {
        public final String patternVarName;
        public final ClassReference targetType;
        public final RuntimeExpr object;

        public CastCheck(String patternVarName, ClassReference targetType, RuntimeExpr object) {
            this.patternVarName = patternVarName;
            this.targetType = targetType;
            this.object = object;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCastCheckExpr(this);
        }
    }

    public static class StaticGet extends RuntimeExpr {
        public final String name;
        public final ClassReference target;

        public StaticGet(String name, ClassReference target) {
            this.name = name;
            this.target = target;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticGetExpr(this);
        }
    }

    public static class Switch extends RuntimeExpr {
        public final RuntimeExpr provider;
        public final RuntimeExpr defaulted;
        public final Map<Object,RuntimeExpr> params;
        public final RuntimeToken keyword;

        public Switch(RuntimeExpr provider, RuntimeExpr defaulted, Map<Object,RuntimeExpr> params, RuntimeToken keyword) {
            this.provider = provider;
            this.defaulted = defaulted;
            this.params = params;
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchExpr(this);
        }
    }

    public static class Slice extends RuntimeExpr {
        public final RuntimeExpr start;
        public final RuntimeExpr end;
        public final RuntimeExpr interval;
        public final RuntimeExpr object;

        public Slice(RuntimeExpr start, RuntimeExpr end, RuntimeExpr interval, RuntimeExpr object) {
            this.start = start;
            this.end = end;
            this.interval = interval;
            this.object = object;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSliceExpr(this);
        }
    }

    public static class Get extends RuntimeExpr {
        public final String name;
        public final RuntimeExpr object;

        public Get(String name, RuntimeExpr object) {
            this.name = name;
            this.object = object;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    public static class ArrayGet extends RuntimeExpr {
        public final RuntimeExpr index;
        public final RuntimeExpr object;

        public ArrayGet(RuntimeExpr index, RuntimeExpr object) {
            this.index = index;
            this.object = object;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayGetExpr(this);
        }
    }

    public static class Literal extends RuntimeExpr {
        public final LiteralHolder token;

        public Literal(LiteralHolder token) {
            this.token = token;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Assign extends RuntimeExpr {
        public final ClassReference executor;
        public final RuntimeToken name;
        public final TokenType type;
        public final RuntimeExpr value;
        public final Operand operand;
        public final int line;

        public Assign(ClassReference executor, RuntimeToken name, TokenType type, RuntimeExpr value, Operand operand, int line) {
            this.executor = executor;
            this.name = name;
            this.type = type;
            this.value = value;
            this.operand = operand;
            this.line = line;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class StaticCall extends RuntimeExpr {
        public final RuntimeExpr[] args;
        public final RuntimeToken name;
        public final ClassReference target;
        public final int methodOrdinal;

        public StaticCall(RuntimeExpr[] args, RuntimeToken name, ClassReference target, int methodOrdinal) {
            this.args = args;
            this.name = name;
            this.target = target;
            this.methodOrdinal = methodOrdinal;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticCallExpr(this);
        }
    }

    public static class Binary extends RuntimeExpr {
        public final RuntimeExpr left;
        public final ClassReference executor;
        public final RuntimeExpr right;
        public final TokenType operator;
        public final int line;
        public final Operand operand;

        public Binary(RuntimeExpr left, ClassReference executor, RuntimeExpr right, TokenType operator, int line, Operand operand) {
            this.left = left;
            this.executor = executor;
            this.right = right;
            this.operator = operator;
            this.line = line;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class StaticSpecial extends RuntimeExpr {
        public final String name;
        public final TokenType assignType;
        public final ClassReference target;

        public StaticSpecial(String name, TokenType assignType, ClassReference target) {
            this.name = name;
            this.assignType = assignType;
            this.target = target;
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
