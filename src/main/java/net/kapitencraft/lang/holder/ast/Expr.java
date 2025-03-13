package net.kapitencraft.lang.holder.ast;

import java.util.List;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.algebra.Operand;
import java.util.Map;
import net.kapitencraft.lang.holder.LiteralHolder;

public abstract class Expr {

    public interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitSpecialAssignExpr(SpecialAssign expr);
        R visitBinaryExpr(Binary expr);
        R visitWhenExpr(When expr);
        R visitInstCallExpr(InstCall expr);
        R visitStaticCallExpr(StaticCall expr);
        R visitGetExpr(Get expr);
        R visitStaticGetExpr(StaticGet expr);
        R visitArrayGetExpr(ArrayGet expr);
        R visitSetExpr(Set expr);
        R visitStaticSetExpr(StaticSet expr);
        R visitArraySetExpr(ArraySet expr);
        R visitSpecialSetExpr(SpecialSet expr);
        R visitStaticSpecialExpr(StaticSpecial expr);
        R visitArraySpecialExpr(ArraySpecial expr);
        R visitSliceExpr(Slice expr);
        R visitSwitchExpr(Switch expr);
        R visitCastCheckExpr(CastCheck expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitUnaryExpr(Unary expr);
        R visitVarRefExpr(VarRef expr);
        R visitConstructorExpr(Constructor expr);
    }

    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;
        public final Token type;
        public final ClassReference executor;
        public final Operand operand;

        public Assign(Token name, Expr value, Token type, ClassReference executor, Operand operand) {
            this.name = name;
            this.value = value;
            this.type = type;
            this.executor = executor;
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class SpecialAssign extends Expr {
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

    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final ClassReference executor;
        public final Operand operand;
        public final Expr right;

        public Binary(Expr left, Token operator, ClassReference executor, Operand operand, Expr right) {
            this.left = left;
            this.operator = operator;
            this.executor = executor;
            this.operand = operand;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class When extends Expr {
        public final Expr condition;
        public final Expr ifTrue;
        public final Expr ifFalse;

        public When(Expr condition, Expr ifTrue, Expr ifFalse) {
            this.condition = condition;
            this.ifTrue = ifTrue;
            this.ifFalse = ifFalse;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhenExpr(this);
        }
    }

    public static class InstCall extends Expr {
        public final Expr callee;
        public final Token name;
        public final int methodOrdinal;
        public final List<Expr> args;

        public InstCall(Expr callee, Token name, int methodOrdinal, List<Expr> args) {
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

    public static class StaticCall extends Expr {
        public final ClassReference target;
        public final Token name;
        public final int methodOrdinal;
        public final List<Expr> args;

        public StaticCall(ClassReference target, Token name, int methodOrdinal, List<Expr> args) {
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

    public static class Get extends Expr {
        public final Expr object;
        public final Token name;

        public Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    public static class StaticGet extends Expr {
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

    public static class ArrayGet extends Expr {
        public final Expr object;
        public final Expr index;

        public ArrayGet(Expr object, Expr index) {
            this.object = object;
            this.index = index;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayGetExpr(this);
        }
    }

    public static class Set extends Expr {
        public final Expr object;
        public final Token name;
        public final Expr value;
        public final Token assignType;
        public final ClassReference executor;
        public final Operand operand;

        public Set(Expr object, Token name, Expr value, Token assignType, ClassReference executor, Operand operand) {
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

    public static class StaticSet extends Expr {
        public final ClassReference target;
        public final Token name;
        public final Expr value;
        public final Token assignType;
        public final ClassReference executor;
        public final Operand operand;

        public StaticSet(ClassReference target, Token name, Expr value, Token assignType, ClassReference executor, Operand operand) {
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

    public static class ArraySet extends Expr {
        public final Expr object;
        public final Expr index;
        public final Expr value;
        public final Token assignType;
        public final ClassReference executor;
        public final Operand operand;

        public ArraySet(Expr object, Expr index, Expr value, Token assignType, ClassReference executor, Operand operand) {
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

    public static class SpecialSet extends Expr {
        public final Expr callee;
        public final Token name;
        public final Token assignType;

        public SpecialSet(Expr callee, Token name, Token assignType) {
            this.callee = callee;
            this.name = name;
            this.assignType = assignType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialSetExpr(this);
        }
    }

    public static class StaticSpecial extends Expr {
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

    public static class ArraySpecial extends Expr {
        public final Expr object;
        public final Expr index;
        public final Token assignType;

        public ArraySpecial(Expr object, Expr index, Token assignType) {
            this.object = object;
            this.index = index;
            this.assignType = assignType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArraySpecialExpr(this);
        }
    }

    public static class Slice extends Expr {
        public final Expr object;
        public final Expr start;
        public final Expr end;
        public final Expr interval;

        public Slice(Expr object, Expr start, Expr end, Expr interval) {
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

    public static class Switch extends Expr {
        public final Expr provider;
        public final Map<Object,Expr> params;
        public final Expr defaulted;
        public final Token keyword;

        public Switch(Expr provider, Map<Object,Expr> params, Expr defaulted, Token keyword) {
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

    public static class CastCheck extends Expr {
        public final Expr object;
        public final ClassReference targetType;
        public final Token patternVarName;

        public CastCheck(Expr object, ClassReference targetType, Token patternVarName) {
            this.object = object;
            this.targetType = targetType;
            this.patternVarName = patternVarName;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCastCheckExpr(this);
        }
    }

    public static class Grouping extends Expr {
        public final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Literal extends Expr {
        public final Token token;

        public Literal(Token token) {
            this.token = token;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Logical extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    public static class Unary extends Expr {
        public final Token operator;
        public final Expr right;

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class VarRef extends Expr {
        public final Token name;

        public VarRef(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarRefExpr(this);
        }
    }

    public static class Constructor extends Expr {
        public final Token keyword;
        public final ClassReference target;
        public final List<Expr> params;
        public final int ordinal;

        public Constructor(Token keyword, ClassReference target, List<Expr> params, int ordinal) {
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

  public abstract <R> R accept(Visitor<R> visitor);
}
