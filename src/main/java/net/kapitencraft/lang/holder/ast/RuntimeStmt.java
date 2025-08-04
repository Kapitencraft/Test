package net.kapitencraft.lang.holder.ast;

import net.kapitencraft.lang.holder.token.RuntimeToken;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.tool.Pair;

public interface RuntimeStmt {

    interface Visitor<R> {
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

    <R> R accept(Visitor<R> visitor);

    record Return(
        RuntimeExpr value
    ) implements RuntimeStmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    record Expression(
        RuntimeExpr expression
    ) implements RuntimeStmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    record VarDecl(
        String name, 
        ClassReference type, 
        RuntimeExpr initializer, 
        boolean isFinal
    ) implements RuntimeStmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarDeclStmt(this);
        }
    }

    record Throw(
        int line, 
        RuntimeExpr value
    ) implements RuntimeStmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThrowStmt(this);
        }
    }

    record For(
        RuntimeStmt init, 
        RuntimeExpr condition, 
        RuntimeExpr increment, 
        RuntimeStmt body
    ) implements RuntimeStmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }
    }

    record Block(
        RuntimeStmt[] statements
    ) implements RuntimeStmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    record Try(
        Block body, 
        Pair<Pair<ClassReference[],String>,Block>[] catches, 
        Block finale
    ) implements RuntimeStmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTryStmt(this);
        }
    }

    record While(
        RuntimeExpr condition, 
        RuntimeStmt body
    ) implements RuntimeStmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    record If(
        RuntimeExpr condition, 
        RuntimeStmt thenBranch, 
        RuntimeStmt elseBranch, 
        Pair<RuntimeExpr,RuntimeStmt>[] elifs
    ) implements RuntimeStmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    record ForEach(
        ClassReference type, 
        String name, 
        RuntimeExpr initializer, 
        RuntimeStmt body
    ) implements RuntimeStmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForEachStmt(this);
        }
    }

    record LoopInterruption(
        TokenType type
    ) implements RuntimeStmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLoopInterruptionStmt(this);
        }
    }
}
