package net.kapitencraft.lang.holder.ast;

import java.util.Map;
import java.util.List;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.tool.Pair;

public abstract class Stmt {

    public interface Visitor<R> {
        R visitImportStmt(Import stmt);
        R visitBlockStmt(Block stmt);
        R visitClassStmt(Class stmt);
        R visitExpressionStmt(Expression stmt);
        R visitFuncDeclStmt(FuncDecl stmt);
        R visitIfStmt(If stmt);
        R visitReturnStmt(Return stmt);
        R visitVarDeclStmt(VarDecl stmt);
        R visitWhileStmt(While stmt);
        R visitForStmt(For stmt);
        R visitLoopInterruptionStmt(LoopInterruption stmt);
    }

    public static class Import extends Stmt {
        public final Expr.ClassRef ref;

        public Import(Expr.ClassRef ref) {
            this.ref = ref;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitImportStmt(this);
        }
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

    public static class Class extends Stmt {
        public final Token name;
        public final List<Stmt.FuncDecl> methods;
        public final List<Stmt.VarDecl> fields;

        public Class(Token name, List<Stmt.FuncDecl> methods, List<Stmt.VarDecl> fields) {
            this.name = name;
            this.methods = methods;
            this.fields = fields;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStmt(this);
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

    public static class FuncDecl extends Stmt {
        public final Token retType;
        public final Token name;
        public final Token end;
        public final List<Pair<Token,Token>> params;
        public final Stmt body;
        public final boolean isFinal;

        public FuncDecl(Token retType, Token name, Token end, List<Pair<Token,Token>> params, Stmt body, boolean isFinal) {
            this.retType = retType;
            this.name = name;
            this.end = end;
            this.params = params;
            this.body = body;
            this.isFinal = isFinal;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFuncDeclStmt(this);
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

    public static class VarDecl extends Stmt {
        public final Token name;
        public final Token type;
        public final Expr initializer;
        public final boolean isFinal;

        public VarDecl(Token name, Token type, Expr initializer, boolean isFinal) {
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

  public abstract <R> R accept(Visitor<R> visitor);
}
