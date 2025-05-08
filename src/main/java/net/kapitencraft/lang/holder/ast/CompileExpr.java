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
        public final byte ordinal;

        public VarRef(Token name, byte ordinal) {
            this.name = name;
            this.ordinal = ordinal;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarRefExpr(this);
        }
    }

    public static class Set extends CompileExpr {
        public final CompileExpr object;
        public final Token name;
        public final CompileExpr value;
        public final Token assignType;
        public final ClassReference executor;
        public final Operand operand;

        public Set(CompileExpr object, Token name, CompileExpr value, Token assignType, ClassReference executor, Operand operand) {
            this.object = object;
            this.name = name;
            this.value = value;
            this.assignType = assignType;
            this.executor = executor;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    public static class ArraySet extends CompileExpr {
        public final CompileExpr object;
        public final CompileExpr index;
        public final CompileExpr value;
        public final Token assignType;
        public final ClassReference executor;
        public final Operand operand;

        public ArraySet(CompileExpr object, CompileExpr index, CompileExpr value, Token assignType, ClassReference executor, Operand operand) {
            this.object = object;
            this.index = index;
            this.value = value;
            this.assignType = assignType;
            this.executor = executor;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySetExpr(this);
        }
    }

    public static class ArraySpecial extends CompileExpr {
        public final CompileExpr object;
        public final CompileExpr index;
        public final Token assignType;

        public ArraySpecial(CompileExpr object, CompileExpr index, Token assignType) {
            this.object = object;
            this.index = index;
            this.assignType = assignType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySpecialExpr(this);
        }
    }

    public static class SpecialAssign extends CompileExpr {
        public final Token name;
        public final Token assignType;
        public final byte ordinal;

        public SpecialAssign(Token name, Token assignType, byte ordinal) {
            this.name = name;
            this.assignType = assignType;
            this.ordinal = ordinal;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialAssignExpr(this);
        }
    }

    public static class Constructor extends CompileExpr {
        public final Token keyword;
        public final ClassReference target;
        public final CompileExpr[] params;
        public final int ordinal;

        public Constructor(Token keyword, ClassReference target, CompileExpr[] params, int ordinal) {
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

    public static class InstCall extends CompileExpr {
        public final CompileExpr callee;
        public final Token name;
        public final int methodOrdinal;
        public final CompileExpr[] args;
        public final ClassReference retType;

        public InstCall(CompileExpr callee, Token name, int methodOrdinal, CompileExpr[] args, ClassReference retType) {
            this.callee = callee;
            this.name = name;
            this.methodOrdinal = methodOrdinal;
            this.args = args;
            this.retType = retType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitInstCallExpr(this);
        }
    }

    public static class StaticSet extends CompileExpr {
        public final ClassReference target;
        public final Token name;
        public final CompileExpr value;
        public final Token assignType;
        public final ClassReference executor;
        public final Operand operand;

        public StaticSet(ClassReference target, Token name, CompileExpr value, Token assignType, ClassReference executor, Operand operand) {
            this.target = target;
            this.name = name;
            this.value = value;
            this.assignType = assignType;
            this.executor = executor;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticSetExpr(this);
        }
    }

    public static class Logical extends CompileExpr {
        public final CompileExpr left;
        public final Token operator;
        public final CompileExpr right;

        public Logical(CompileExpr left, Token operator, CompileExpr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
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
        public final Token operator;
        public final CompileExpr right;

        public Unary(Token operator, CompileExpr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class When extends CompileExpr {
        public final CompileExpr condition;
        public final CompileExpr ifTrue;
        public final CompileExpr ifFalse;

        public When(CompileExpr condition, CompileExpr ifTrue, CompileExpr ifFalse) {
            this.condition = condition;
            this.ifTrue = ifTrue;
            this.ifFalse = ifFalse;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhenExpr(this);
        }
    }

    public static class CastCheck extends CompileExpr {
        public final CompileExpr object;
        public final ClassReference targetType;
        public final Token patternVarName;

        public CastCheck(CompileExpr object, ClassReference targetType, Token patternVarName) {
            this.object = object;
            this.targetType = targetType;
            this.patternVarName = patternVarName;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCastCheckExpr(this);
        }
    }

    public static class StaticGet extends CompileExpr {
        public final ClassReference target;
        public final Token name;

        public StaticGet(ClassReference target, Token name) {
            this.target = target;
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticGetExpr(this);
        }
    }

    public static class Switch extends CompileExpr {
        public final CompileExpr provider;
        public final Map<Object,CompileExpr> params;
        public final CompileExpr defaulted;
        public final Token keyword;

        public Switch(CompileExpr provider, Map<Object,CompileExpr> params, CompileExpr defaulted, Token keyword) {
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

    public static class Slice extends CompileExpr {
        public final CompileExpr object;
        public final CompileExpr start;
        public final CompileExpr end;
        public final CompileExpr interval;

        public Slice(CompileExpr object, CompileExpr start, CompileExpr end, CompileExpr interval) {
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

    public static class Get extends CompileExpr {
        public final CompileExpr object;
        public final Token name;

        public Get(CompileExpr object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    public static class ArrayGet extends CompileExpr {
        public final CompileExpr object;
        public final CompileExpr index;

        public ArrayGet(CompileExpr object, CompileExpr index) {
            this.object = object;
            this.index = index;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayGetExpr(this);
        }
    }

    public static class Literal extends CompileExpr {
        public final Token literal;

        public Literal(Token literal) {
            this.literal = literal;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Assign extends CompileExpr {
        public final Token name;
        public final CompileExpr value;
        public final Token type;
        public final ClassReference executor;
        public final Operand operand;
        public final byte ordinal;

        public Assign(Token name, CompileExpr value, Token type, ClassReference executor, Operand operand, byte ordinal) {
            this.name = name;
            this.value = value;
            this.type = type;
            this.executor = executor;
            this.operand = operand;
            this.ordinal = ordinal;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class StaticCall extends CompileExpr {
        public final ClassReference target;
        public final Token name;
        public final int methodOrdinal;
        public final CompileExpr[] args;
        public final ClassReference retType;

        public StaticCall(ClassReference target, Token name, int methodOrdinal, CompileExpr[] args, ClassReference retType) {
            this.target = target;
            this.name = name;
            this.methodOrdinal = methodOrdinal;
            this.args = args;
            this.retType = retType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitStaticCallExpr(this);
        }
    }

    public static class Binary extends CompileExpr {
        public final CompileExpr left;
        public final CompileExpr right;
        public final Token operator;
        public final ClassReference executor;
        public final Operand operand;

        public Binary(CompileExpr left, CompileExpr right, Token operator, ClassReference executor, Operand operand) {
            this.left = left;
            this.right = right;
            this.operator = operator;
            this.executor = executor;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class StaticSpecial extends CompileExpr {
        public final ClassReference target;
        public final Token name;
        public final Token assignType;

        public StaticSpecial(ClassReference target, Token name, Token assignType) {
            this.target = target;
            this.name = name;
            this.assignType = assignType;
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
