package net.kapitencraft.lang.holder.ast;

import java.util.Map;
import java.util.List;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.lang.oop.clazz.LoxClass;

public abstract class Stmt {

    public interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitExpressionStmt(Expression stmt);
        R visitIfStmt(If stmt);
        R visitReturnStmt(Return stmt);
        R visitThrowStmt(Throw stmt);
        R visitVarDeclStmt(VarDecl stmt);
        R visitWhileStmt(While stmt);
        R visitForStmt(For stmt);
        R visitLoopInterruptionStmt(LoopInterruption stmt);
        R visitTryStmt(Try stmt);
    }

    public static class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
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
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
        public final List<Pair<Expr,Stmt>> elifs;
        public final Token keyword;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch, List<Pair<Expr,Stmt>> elifs, Token keyword) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
            this.elifs = elifs;
            this.keyword = keyword;
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
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    public static class Throw extends Stmt {
        public final Token keyword;
        public final Expr value;

        public Throw(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThrowStmt(this);
        }
    }

    public static class VarDecl extends Stmt {
        public final Token name;
        public final LoxClass type;
        public final Expr initializer;
        public final boolean isFinal;

        public VarDecl(Token name, LoxClass type, Expr initializer, boolean isFinal) {
            this.name = name;
            this.type = type;
            this.initializer = initializer;
            this.isFinal = isFinal;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarDeclStmt(this);
        }
    }

    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt body;
        public final Token keyword;

        public While(Expr condition, Stmt body, Token keyword) {
            this.condition = condition;
            this.body = body;
            this.keyword = keyword;
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
        public final Token keyword;

        public For(Stmt init, Expr condition, Expr increment, Stmt body, Token keyword) {
            this.init = init;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
            this.keyword = keyword;
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
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLoopInterruptionStmt(this);
        }
    }

    public static class Try extends Stmt {
        public final Block body;
        public final List<Pair<Pair<List<LoxClass>,Token>,Block>> catches;
        public final Block finale;

        public Try(Block body, List<Pair<Pair<List<LoxClass>,Token>,Block>> catches, Block finale) {
            this.body = body;
            this.catches = catches;
            this.finale = finale;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTryStmt(this);
        }
    }

  public abstract <R> R accept(Visitor<R> visitor);
}
