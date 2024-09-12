package net.kapitencraft.lang.ast;

import java.util.List;
import net.kapitencraft.lang.ast.token.Token;
import net.kapitencraft.tool.Pair;

public abstract class Stmt {

    public interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitExpressionStmt(Expression stmt);
        R visitFunctionStmt(Function stmt);
        R visitIfStmt(If stmt);
        R visitReturnStmt(Return stmt);
        R visitVarStmt(Var stmt);
        R visitWhileStmt(While stmt);
        R visitForStmt(For stmt);
        R visitLoopInterruptionStmt(LoopInterruption stmt);
    }

    public static class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public Token location() {
            return statements.get(0).location();
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    public static class Expression extends Stmt {
        public final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        public Token location() {
            return expression.location();
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    public static class Function extends Stmt {
        public final Token retType;
        public final Token name;
        public final List<Pair<Token,Token>> params;
        public final List<Stmt> body;

        public Function(Token retType, Token name, List<Pair<Token,Token>> params, List<Stmt> body) {
            this.retType = retType;
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public Token location() {
            return retType;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
        private final Token keyword;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch, Token keyword) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
            this.keyword = keyword;
        }

        @Override
        public Token location() {
            return keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    public static class Return extends Stmt {
        public final Token keyword;
        public final Expr value;

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public Token location() {
            return keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    public static class Var extends Stmt {
        public final Token name;
        public final Token type;
        public final Expr initializer;

        public Var(Token name, Token type, Expr initializer) {
            this.name = name;
            this.type = type;
            this.initializer = initializer;
        }

        @Override
        public Token location() {
            return type;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt body;
        private final Token keyword;

        public While(Expr condition, Stmt body, Token keyword) {
            this.condition = condition;
            this.body = body;
            this.keyword = keyword;
        }

        @Override
        public Token location() {
            return keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    public static class For extends Stmt {
        public final Stmt init;
        public final Expr condition;
        public final Expr increment;
        public final Stmt body;
        private final Token keyword;

        public For(Stmt init, Expr condition, Expr increment, Stmt body, Token keyword) {
            this.init = init;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
            this.keyword = keyword;
        }

        @Override
        public Token location() {
            return null;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }
    }

    public static class LoopInterruption extends Stmt {
        public final Token type;

        public LoopInterruption(Token type) {
            this.type = type;
        }

        @Override
        public Token location() {
            return type;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLoopInterruptionStmt(this);
        }
    }

    public abstract <R> R accept(Visitor<R> visitor);
    public abstract Token location();
}
