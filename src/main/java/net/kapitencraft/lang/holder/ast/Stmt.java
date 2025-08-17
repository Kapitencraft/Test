package net.kapitencraft.lang.holder.ast;

import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.tool.Pair;

public interface Stmt {

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
        Token keyword, 
        Expr value
    ) implements Stmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    record Expression(
        Expr expression
    ) implements Stmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    record VarDecl(
        Token name, 
        ClassReference type, 
        Expr initializer, 
        boolean isFinal
    ) implements Stmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarDeclStmt(this);
        }
    }

    record Throw(
        Token keyword, 
        Expr value
    ) implements Stmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThrowStmt(this);
        }
    }

    record For(
        Stmt init, 
        Expr condition, 
        Expr increment, 
        Stmt body, 
        Token keyword
    ) implements Stmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }
    }

    record Block(
        Stmt[] statements
    ) implements Stmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    record Try(
        Block body, 
        Pair<Pair<ClassReference[],Token>,Block>[] catches, 
        Block finale
    ) implements Stmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTryStmt(this);
        }
    }

    record While(
        Expr condition, 
        Stmt body, 
        Token keyword
    ) implements Stmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    record If(
        Expr condition, 
        Stmt thenBranch, 
        Stmt elseBranch, 
        Pair<Expr,Stmt>[] elifs, 
        Token keyword
    ) implements Stmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    record ForEach(
        ClassReference type, 
        Token name, 
        Expr initializer, 
        Stmt body, 
        int baseVar
    ) implements Stmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForEachStmt(this);
        }
    }

    record LoopInterruption(
        Token type
    ) implements Stmt {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLoopInterruptionStmt(this);
        }
    }
}
