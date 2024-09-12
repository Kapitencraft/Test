package net.kapitencraft.lang.ast;

import java.util.List;
import net.kapitencraft.lang.ast.token.Token;

public abstract class Expr {

    public interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitSpecialAssignExpr(SpecialAssign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitUnaryExpr(Unary expr);
        R visitVariableExpr(Variable expr);
        R visitFunctionExpr(Function expr);
    }

    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;
        public final Token type;

        public Assign(Token name, Expr value, Token type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        @Override
        public Token location() {
            return name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class SpecialAssign extends Expr {
        public final Token name;
        public final Token type;

        public SpecialAssign(Token name, Token type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public Token location() {
            return name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSpecialAssignExpr(this);
        }
    }

    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public Token location() {
            return left.location();
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class Call extends Expr {
        public final Expr callee;
        public final Token args;
        public final List<Expr> arguments;

        public Call(Expr callee, Token args, List<Expr> arguments) {
            this.callee = callee;
            this.args = args;
            this.arguments = arguments;
        }

        @Override
        public Token location() {
            return null;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    public static class Grouping extends Expr {
        public final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        public Token location() {
            return expression.location();
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Literal extends Expr {
        private final Token literal;
        public final Object value;

        public Literal(Token literal, Object value) {
            this.literal = literal;
            this.value = value;
        }

        @Override
        public Token location() {
            return literal;
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
        public Token location() {
            return operator;
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
        public Token location() {
            return operator;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class Variable extends Expr {
        public final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        @Override
        public Token location() {
            return name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    public static class Function extends Expr {
        public final Token name;

        public Function(Token name) {
            this.name = name;
        }

        @Override
        public Token location() {
            return name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionExpr(this);
        }
    }

    public abstract <R> R accept(Visitor<R> visitor);
    public abstract Token location();
}
