package net.kapitencraft.scripted.lang.compiler.ast_optimize.impl;

import net.kapitencraft.scripted.lang.compiler.ast_optimize.ASTOptimization;
import net.kapitencraft.scripted.lang.holder.ast.Expr;
import net.kapitencraft.scripted.lang.holder.ast.Stmt;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CleanUnusedLocalsOptimization implements ASTOptimization {

    //TODO

    @Override
    public void optimize(List<Stmt> code) {
        VarUseFinder finder = new VarUseFinder();
        int i = 0;
        List<Integer> used = new ArrayList<>();
        while (i < code.size()) {
            Stmt stmt = code.get(i);
            if (stmt instanceof Stmt.VarDecl varDecl) {
                finder.setTargetName(varDecl.name.lexeme());
                int j = i + 1;
                while (j < code.size()) {
                    if (finder.check(code.get(j))) {
                        used.add(varDecl.localId);
                        break;
                    }
                    j++;
                }
                if (j == code.size()) {
                    //unused can be removed
                    if (varDecl.initializer != null) {
                        Stmt.Expression expression = new Stmt.Expression();
                        expression.expression = varDecl.initializer;
                        code.set(i, expression);
                    } else {
                        code.remove(i);
                    }
                }
            } else
                optimizeRecursive(stmt);
            i++;
        }
        int[] mapper = new int[used.getLast()];
        int lI = 0;
        //'used' array is in order of var creation as code was searched linearly. locals can therefore be ordered ascending
        for (Integer integer : used) {
            mapper[integer] = lI++;
        }

    }

    private static class VarUseFinder implements Expr.Visitor<Boolean>, Stmt.Visitor<Boolean> {
        private String targetName;

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public boolean check(Stmt stmt) {
            return stmt.accept(this);
        }

        public boolean check(Expr expr) {
            return expr.accept(this);
        }

        @Override
        public Boolean visitCallExpr(Expr.Call expr) {
            return expr.object != null && check(expr.object) || Arrays.stream(expr.args).anyMatch(this::check);
        }

        @Override
        public Boolean visitSetExpr(Expr.Set expr) {
            return this.check(expr.value) || this.check(expr.object);
        }

        @Override
        public Boolean visitArraySetExpr(Expr.ArraySet expr) {
            return check(expr.index) || check(expr.object) || check(expr.value);
        }

        @Override
        public Boolean visitArraySpecialExpr(Expr.ArraySpecial expr) {
            return check(expr.index) || check(expr.object);
        }

        @Override
        public Boolean visitSingleIdentifierExpr(Expr.SingleIdentifier expr) {
            return expr.type == null && expr.name.lexeme().equals(targetName);
        }

        @Override
        public Boolean visitConstructorExpr(Expr.Constructor expr) {
            return Arrays.stream(expr.args).anyMatch(this::check);
        }

        @Override
        public Boolean visitStaticSetExpr(Expr.StaticSet expr) {
            return check(expr.value);
        }

        @Override
        public Boolean visitLogicalExpr(Expr.Logical expr) {
            return check(expr.left) || check(expr.right);
        }

        @Override
        public Boolean visitIdentifierSpecialAssignExpr(Expr.IdentifierSpecialAssign expr) {
            return false;
        }

        @Override
        public Boolean visitUnaryExpr(Expr.Unary expr) {
            return check(expr.right);
        }

        @Override
        public Boolean visitComparisonChainExpr(Expr.ComparisonChain expr) {
            return Arrays.stream(expr.entries).anyMatch(this::check);
        }

        @Override
        public Boolean visitWhenExpr(Expr.When expr) {
            return check(expr.condition) || check(expr.ifTrue) || check(expr.ifFalse);
        }

        @Override
        public Boolean visitCastCheckExpr(Expr.CastCheck expr) {
            return check(expr.object);
        }

        @Override
        public Boolean visitStaticGetExpr(Expr.StaticGet expr) {
            return false;
        }

        @Override
        public Boolean visitSwitchExpr(Expr.Switch expr) {
            return check(expr.defaulted) || check(expr.provider) || Arrays.stream(expr.params)
                    .map(k -> k.expr).anyMatch(this::check);
        }

        @Override
        public Boolean visitIdentifierAssignExpr(Expr.IdentifierAssign expr) {
            //only accessed when modifying the variable, access can be ignored
            return check(expr.value);
        }

        @Override
        public Boolean visitSliceExpr(Expr.Slice expr) {
            return check(expr.object) ||
                    expr.start != null && check(expr.start) ||
                    expr.end != null && check(expr.end) ||
                    expr.interval != null && check(expr.interval);
        }

        @Override
        public Boolean visitGetExpr(Expr.Get expr) {
            return check(expr.object);
        }

        @Override
        public Boolean visitArrayGetExpr(Expr.ArrayGet expr) {
            return check(expr.object) || check(expr.index);
        }

        @Override
        public Boolean visitLiteralExpr(Expr.Literal expr) {
            return false;
        }

        @Override
        public Boolean visitArrayConstructorExpr(Expr.ArrayConstructor expr) {
            return expr.size != null ? check(expr.size) : Arrays.stream(expr.obj).anyMatch(this::check);
        }

        @Override
        public Boolean visitBinaryExpr(Expr.Binary expr) {
            return check(expr.left) || check(expr.right);
        }

        @Override
        public Boolean visitStaticSpecialExpr(Expr.StaticSpecial expr) {
            return false;
        }

        @Override
        public Boolean visitSpecialSetExpr(Expr.SpecialSet expr) {
            return false;
        }

        @Override
        public Boolean visitReturnStmt(Stmt.Return stmt) {
            return stmt.value != null && check(stmt.value);
        }

        @Override
        public Boolean visitForStmt(Stmt.For stmt) {
            return check(stmt.condition) || check(stmt.body) || (stmt.increment != null && check(stmt.increment));
        }

        @Override
        public Boolean visitWhileStmt(Stmt.While stmt) {
            return check(stmt.condition) || check(stmt.body);
        }

        @Override
        public Boolean visitForEachStmt(Stmt.ForEach stmt) {
            return check(stmt.initializer) || check(stmt.body);
        }

        @Override
        public Boolean visitDebugTraceStmt(Stmt.DebugTrace stmt) {
            return false;
        }

        @Override
        public Boolean visitExpressionStmt(Stmt.Expression stmt) {
            return check(stmt.expression);
        }

        @Override
        public Boolean visitVarDeclStmt(Stmt.VarDecl stmt) {
            return stmt.initializer != null && check(stmt.initializer);
        }

        @Override
        public Boolean visitThrowStmt(Stmt.Throw stmt) {
            return check(stmt.value);
        }

        @Override
        public Boolean visitBlockStmt(Stmt.Block stmt) {
            return stmt.statements.stream().anyMatch(this::check);
        }

        @Override
        public Boolean visitTryStmt(Stmt.Try stmt) {
            return check(stmt.body) || check(stmt.finale) || Arrays.stream(stmt.catches).map(Pair::getSecond).anyMatch(this::check);
        }

        @Override
        public Boolean visitClearLocalsStmt(Stmt.ClearLocals stmt) {
            return false;
        }

        @Override
        public Boolean visitIfStmt(Stmt.If stmt) {
            return check(stmt.condition) || check(stmt.thenBranch) ||
                    stmt.elseBranch != null && check(stmt.elseBranch) ||
                    Arrays.stream(stmt.elifs).anyMatch(b -> check(b.condition) || check(b.body));
        }

        @Override
        public Boolean visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
            return false;
        }
    }
}
