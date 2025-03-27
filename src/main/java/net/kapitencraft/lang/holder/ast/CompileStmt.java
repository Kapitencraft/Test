package net.kapitencraft.lang.holder.ast;

import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.tool.Pair;

public abstract class CompileStmt {

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

    public static class Return extends CompileStmt {
        public final Token keyword;
        public final CompileExpr value;

        public Return(Token keyword, CompileExpr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    public static class Expression extends CompileStmt {
        public final CompileExpr expression;

        public Expression(CompileExpr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    public static class VarDecl extends CompileStmt {
        public final Token name;
        public final ClassReference type;
        public final CompileExpr initializer;
        public final boolean isFinal;

        public VarDecl(Token name, ClassReference type, CompileExpr initializer, boolean isFinal) {
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

    public static class Throw extends CompileStmt {
        public final Token keyword;
        public final CompileExpr value;

        public Throw(Token keyword, CompileExpr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThrowStmt(this);
        }
    }

    public static class For extends CompileStmt {
        public final CompileStmt init;
        public final CompileExpr condition;
        public final CompileExpr increment;
        public final CompileStmt body;
        public final Token keyword;

        public For(CompileStmt init, CompileExpr condition, CompileExpr increment, CompileStmt body, Token keyword) {
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

    public static class Block extends CompileStmt {
        public final CompileStmt[] statements;

        public Block(CompileStmt[] statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    public static class Try extends CompileStmt {
        public final Block body;
        public final Pair<Pair<ClassReference[],Token>,Block>[] catches;
        public final Block finale;

        public Try(Block body, Pair<Pair<ClassReference[],Token>,Block>[] catches, Block finale) {
            this.body = body;
            this.catches = catches;
            this.finale = finale;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTryStmt(this);
        }
    }

    public static class While extends CompileStmt {
        public final CompileExpr condition;
        public final CompileStmt body;
        public final Token keyword;

        public While(CompileExpr condition, CompileStmt body, Token keyword) {
            this.condition = condition;
            this.body = body;
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    public static class If extends CompileStmt {
        public final CompileExpr condition;
        public final CompileStmt thenBranch;
        public final CompileStmt elseBranch;
        public final Pair<CompileExpr,CompileStmt>[] elifs;
        public final Token keyword;

        public If(CompileExpr condition, CompileStmt thenBranch, CompileStmt elseBranch, Pair<CompileExpr,CompileStmt>[] elifs, Token keyword) {
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

    public static class ForEach extends CompileStmt {
        public final ClassReference type;
        public final Token name;
        public final CompileExpr initializer;
        public final CompileStmt body;

        public ForEach(ClassReference type, Token name, CompileExpr initializer, CompileStmt body) {
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

    public static class LoopInterruption extends CompileStmt {
        public final Token type;

        public LoopInterruption(Token type) {
            this.type = type;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLoopInterruptionStmt(this);
        }
    }

  public abstract <R> R accept(Visitor<R> visitor);
}
