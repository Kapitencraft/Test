package net.kapitencraft.lang.holder.ast;

import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.algebra.Operand;
import java.util.Map;
import net.kapitencraft.lang.holder.LiteralHolder;

public abstract class CompileExpr {

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

    public static class VarRef extends CompileExpr {
        public final Token name;

        public VarRef(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarRefExpr(this);
        }
    }

    public static class Set extends CompileExpr {
        public final ClassReference executor;
        public final Token name;
        public final Token assignType;
        public final CompileExpr value;
        public final Operand operand;
        public final CompileExpr object;

        public Set(ClassReference executor, Token name, Token assignType, CompileExpr value, Operand operand, CompileExpr object) {
            this.executor = executor;
            this.name = name;
            this.assignType = assignType;
            this.value = value;
            this.operand = operand;
            this.object = object;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    public static class ArraySet extends CompileExpr {
        public final ClassReference executor;
        public final CompileExpr index;
        public final Token assignType;
        public final CompileExpr value;
        public final Operand operand;
        public final CompileExpr object;

        public ArraySet(ClassReference executor, CompileExpr index, Token assignType, CompileExpr value, Operand operand, CompileExpr object) {
            this.executor = executor;
            this.index = index;
            this.assignType = assignType;
            this.value = value;
            this.operand = operand;
            this.object = object;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySetExpr(this);
        }
    }

    public static class ArraySpecial extends CompileExpr {
        public final CompileExpr index;
        public final Token assignType;
        public final CompileExpr object;

        public ArraySpecial(CompileExpr index, Token assignType, CompileExpr object) {
            this.index = index;
            this.assignType = assignType;
            this.object = object;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySpecialExpr(this);
        }
    }

    public static class SpecialAssign extends CompileExpr {
        public final Token name;
        public final Token assignType;

        public SpecialAssign(Token name, Token assignType) {
            this.name = name;
            this.assignType = assignType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialAssignExpr(this);
        }
    }

    public static class Constructor extends CompileExpr {
        public final Token keyword;
        public final CompileExpr[] params;
        public final ClassReference target;
        public final int ordinal;

        public Constructor(Token keyword, CompileExpr[] params, ClassReference target, int ordinal) {
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

    public static class InstCall extends CompileExpr {
        public final CompileExpr[] args;
        public final CompileExpr callee;
        public final Token name;
        public final int methodOrdinal;

        public InstCall(CompileExpr[] args, CompileExpr callee, Token name, int methodOrdinal) {
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

    public static class StaticSet extends CompileExpr {
        public final ClassReference executor;
        public final Token name;
        public final Token assignType;
        public final CompileExpr value;
        public final Operand operand;
        public final ClassReference target;

        public StaticSet(ClassReference executor, Token name, Token assignType, CompileExpr value, Operand operand, ClassReference target) {
            this.executor = executor;
            this.name = name;
            this.assignType = assignType;
            this.value = value;
            this.operand = operand;
            this.target = target;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSetExpr(this);
        }
    }

    public static class Logical extends CompileExpr {
        public final CompileExpr left;
        public final CompileExpr right;
        public final Token operator;

        public Logical(CompileExpr left, CompileExpr right, Token operator) {
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    public static class Grouping extends CompileExpr {
        public final CompileExpr expression;

        public Grouping(CompileExpr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Unary extends CompileExpr {
        public final CompileExpr right;
        public final Token operator;

        public Unary(CompileExpr right, Token operator) {
            this.right = right;
            this.operator = operator;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class When extends CompileExpr {
        public final CompileExpr condition;
        public final CompileExpr ifFalse;
        public final CompileExpr ifTrue;

        public When(CompileExpr condition, CompileExpr ifFalse, CompileExpr ifTrue) {
            this.condition = condition;
            this.ifFalse = ifFalse;
            this.ifTrue = ifTrue;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhenExpr(this);
        }
    }

    public static class CastCheck extends CompileExpr {
        public final Token patternVarName;
        public final ClassReference targetType;
        public final CompileExpr object;

        public CastCheck(Token patternVarName, ClassReference targetType, CompileExpr object) {
            this.patternVarName = patternVarName;
            this.targetType = targetType;
            this.object = object;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCastCheckExpr(this);
        }
    }

    public static class StaticGet extends CompileExpr {
        public final Token name;
        public final ClassReference target;

        public StaticGet(Token name, ClassReference target) {
            this.name = name;
            this.target = target;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticGetExpr(this);
        }
    }

    public static class Switch extends CompileExpr {
        public final CompileExpr provider;
        public final CompileExpr defaulted;
        public final Map<Object,CompileExpr> params;
        public final Token keyword;

        public Switch(CompileExpr provider, CompileExpr defaulted, Map<Object,CompileExpr> params, Token keyword) {
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

    public static class Slice extends CompileExpr {
        public final CompileExpr start;
        public final CompileExpr end;
        public final CompileExpr interval;
        public final CompileExpr object;

        public Slice(CompileExpr start, CompileExpr end, CompileExpr interval, CompileExpr object) {
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

    public static class Get extends CompileExpr {
        public final Token name;
        public final CompileExpr object;

        public Get(Token name, CompileExpr object) {
            this.name = name;
            this.object = object;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    public static class ArrayGet extends CompileExpr {
        public final CompileExpr index;
        public final CompileExpr object;

        public ArrayGet(CompileExpr index, CompileExpr object) {
            this.index = index;
            this.object = object;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayGetExpr(this);
        }
    }

    public static class Literal extends CompileExpr {
        public final Token token;

        public Literal(Token token) {
            this.token = token;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Assign extends CompileExpr {
        public final ClassReference executor;
        public final Token name;
        public final Token type;
        public final CompileExpr value;
        public final Operand operand;

        public Assign(ClassReference executor, Token name, Token type, CompileExpr value, Operand operand) {
            this.executor = executor;
            this.name = name;
            this.type = type;
            this.value = value;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class StaticCall extends CompileExpr {
        public final CompileExpr[] args;
        public final Token name;
        public final ClassReference target;
        public final int methodOrdinal;

        public StaticCall(CompileExpr[] args, Token name, ClassReference target, int methodOrdinal) {
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

    public static class Binary extends CompileExpr {
        public final CompileExpr left;
        public final ClassReference executor;
        public final CompileExpr right;
        public final Token operator;
        public final Operand operand;

        public Binary(CompileExpr left, ClassReference executor, CompileExpr right, Token operator, Operand operand) {
            this.left = left;
            this.executor = executor;
            this.right = right;
            this.operator = operator;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class StaticSpecial extends CompileExpr {
        public final Token name;
        public final Token assignType;
        public final ClassReference target;

        public StaticSpecial(Token name, Token assignType, ClassReference target) {
            this.name = name;
            this.assignType = assignType;
            this.target = target;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSpecialExpr(this);
        }
    }

    public static class SpecialSet extends CompileExpr {
        public final CompileExpr callee;
        public final Token name;
        public final Token assignType;

        public SpecialSet(CompileExpr callee, Token name, Token assignType) {
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
