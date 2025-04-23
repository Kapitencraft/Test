package net.kapitencraft.lang.holder.ast;

import net.kapitencraft.lang.holder.token.RuntimeToken;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.TokenType;
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
        public final RuntimeExpr value;

        public Return(RuntimeExpr value) {
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
        public final String name;
        public final ClassReference type;
        public final RuntimeExpr initializer;
        public final boolean isFinal;

        public VarDecl(String name, ClassReference type, RuntimeExpr initializer, boolean isFinal) {
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

    public static class Throw extends RuntimeStmt {
        public final int line;
        public final RuntimeExpr value;

        public Throw(int line, RuntimeExpr value) {
            this.line = line;
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

        public For(RuntimeStmt init, RuntimeExpr condition, RuntimeExpr increment, RuntimeStmt body) {
            this.init = init;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
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
        public final Block body;
        public final Pair<Pair<ClassReference[],String>,Block>[] catches;
        public final Block finale;

        public Try(Block body, Pair<Pair<ClassReference[],String>,Block>[] catches, Block finale) {
            this.body = body;
            this.catches = catches;
            this.finale = finale;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTryStmt(this);
        }
    }

    public static class While extends RuntimeStmt {
        public final RuntimeExpr condition;
        public final RuntimeStmt body;

        public While(RuntimeExpr condition, RuntimeStmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    public static class If extends RuntimeStmt {
        public final RuntimeExpr condition;
        public final RuntimeStmt thenBranch;
        public final RuntimeStmt elseBranch;
        public final Pair<RuntimeExpr,RuntimeStmt>[] elifs;

        public If(RuntimeExpr condition, RuntimeStmt thenBranch, RuntimeStmt elseBranch, Pair<RuntimeExpr,RuntimeStmt>[] elifs) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
            this.elifs = elifs;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    public static class ForEach extends RuntimeStmt {
        public final ClassReference type;
        public final String name;
        public final RuntimeExpr initializer;
        public final RuntimeStmt body;

        public ForEach(ClassReference type, String name, RuntimeExpr initializer, RuntimeStmt body) {
            this.type = type;
            this.name = name;
            this.initializer = initializer;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForEachStmt(this);
        }
    }

    public static class LoopInterruption extends RuntimeStmt {
        public final TokenType type;

        public LoopInterruption(TokenType type) {
            this.type = type;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLoopInterruptionStmt(this);
        }
    }

  public abstract <R> R accept(Visitor<R> visitor);
}
