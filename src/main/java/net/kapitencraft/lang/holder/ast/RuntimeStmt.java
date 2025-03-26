package net.kapitencraft.lang.holder.ast;

import net.kapitencraft.lang.holder.token.RuntimeToken;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.tool.Pair;

public abstract class RuntimeStmt {

    public interface Visitor<R> {
        R visitReturnStmt(Return stmt);
        R visitExpressionStmt(Expression stmt);
        R visitVarDeclStmt(VarDecl stmt);
        R visitThrowStmt(Throw stmt);
        R visitForStmt(For stmt);
        R visitBlockStmt(Block stmt);
        R visitTryStmt(Try stmt);
        R visitWhileStmt(While stmt);
        R visitIfStmt(If stmt);
        R visitForEachStmt(ForEach stmt);
        R visitLoopInterruptionStmt(LoopInterruption stmt);
    }

    public static class Return extends RuntimeStmt {
        public final RuntimeToken keyword;
        public final RuntimeExpr value;

        public Return(RuntimeToken keyword, RuntimeExpr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    public static class Expression extends RuntimeStmt {
        public final RuntimeExpr expression;

        public Expression(RuntimeExpr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    public static class VarDecl extends RuntimeStmt {
        public final RuntimeToken name;
        public final boolean isFinal;
        public final ClassReference type;
        public final RuntimeExpr initializer;

        public VarDecl(RuntimeToken name, boolean isFinal, ClassReference type, RuntimeExpr initializer) {
            this.name = name;
            this.isFinal = isFinal;
            this.type = type;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarDeclStmt(this);
        }
    }

    public static class Throw extends RuntimeStmt {
        public final RuntimeToken keyword;
        public final RuntimeExpr value;

        public Throw(RuntimeToken keyword, RuntimeExpr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThrowStmt(this);
        }
    }

    public static class For extends RuntimeStmt {
        public final RuntimeStmt init;
        public final RuntimeExpr condition;
        public final RuntimeExpr increment;
        public final RuntimeStmt body;
        public final RuntimeToken keyword;

        public For(RuntimeStmt init, RuntimeExpr condition, RuntimeExpr increment, RuntimeStmt body, RuntimeToken keyword) {
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

    public static class Block extends RuntimeStmt {
        public final RuntimeStmt[] statements;

        public Block(RuntimeStmt[] statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    public static class Try extends RuntimeStmt {
        public final Block finale;
        public final Pair<Pair<ClassReference[],String>,Block>[] catches;
        public final Block body;

        public Try(Block finale, Pair<Pair<ClassReference[],String>,Block>[] catches, Block body) {
            this.finale = finale;
            this.catches = catches;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTryStmt(this);
        }
    }

    public static class While extends RuntimeStmt {
        public final RuntimeExpr condition;
        public final RuntimeStmt body;
        public final RuntimeToken keyword;

        public While(RuntimeExpr condition, RuntimeStmt body, RuntimeToken keyword) {
            this.condition = condition;
            this.body = body;
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    public static class If extends RuntimeStmt {
        public final RuntimeExpr condition;
        public final RuntimeStmt elseBranch;
        public final RuntimeStmt thenBranch;
        public final RuntimeToken keyword;
        public final Pair<RuntimeExpr,RuntimeStmt>[] elifs;

        public If(RuntimeExpr condition, RuntimeStmt elseBranch, RuntimeStmt thenBranch, RuntimeToken keyword, Pair<RuntimeExpr,RuntimeStmt>[] elifs) {
            this.condition = condition;
            this.elseBranch = elseBranch;
            this.thenBranch = thenBranch;
            this.keyword = keyword;
            this.elifs = elifs;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    public static class ForEach extends RuntimeStmt {
        public final String name;
        public final ClassReference type;
        public final RuntimeStmt body;
        public final RuntimeExpr initializer;

        public ForEach(String name, ClassReference type, RuntimeStmt body, RuntimeExpr initializer) {
            this.name = name;
            this.type = type;
            this.body = body;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForEachStmt(this);
        }
    }

    public static class LoopInterruption extends RuntimeStmt {
        public final RuntimeToken type;

        public LoopInterruption(RuntimeToken type) {
            this.type = type;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLoopInterruptionStmt(this);
        }
    }

  public abstract <R> R accept(Visitor<R> visitor);
}
