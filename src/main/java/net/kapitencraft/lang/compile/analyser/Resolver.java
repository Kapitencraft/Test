package net.kapitencraft.lang.compile.analyser;

import net.kapitencraft.lang.ast.Expr;
import net.kapitencraft.lang.ast.Stmt;
import net.kapitencraft.lang.ast.Token;
import net.kapitencraft.lang.ast.TokenType;
import net.kapitencraft.lang.run.Main;

import java.util.List;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final EnvAnalyser analyser = new EnvAnalyser();
    private FunctionType currentFunction = FunctionType.NONE;
    private final String[] lines;

    public Resolver(String[] lines) {
        this.lines = lines;
        analyser.addMethod("clock");
        analyser.addMethod("print");
        analyser.addMethod("input");
        analyser.addMethod("abs");
        analyser.addMethod("randInt");
    }

    private void error(Token token, String message) {
        Main.error(token, message, lines[token.line - 1]);
    }

    private void checkVarExistence(Token name, boolean requireValue) {
        if (!analyser.hasVar(name.lexeme)) {
            error(name, "Variable '" + name.lexeme + "' not defined");
        } else if (requireValue && !analyser.hasVarValue(name.lexeme)) {
            error(name, "Variable '" + name.lexeme + "' might not have been initialized");
        }
    }

    private void createVar(Token name, Token type, boolean hasValue) {
        if (analyser.hasVar(name.lexeme)) {
            error(name, "Variable '" + name.lexeme + "' already defined");
        }
        analyser.addVar(name.lexeme, type.lexeme, hasValue);
    }

    private void define(Token name) {
        checkVarExistence(name, false);
        analyser.setHasVarValue(name.lexeme);
    }

    private enum FunctionType {
        NONE,
        FUNCTION
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    public void resolve(List<Stmt> statements) {

        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        Token name = function.name;
        if (analyser.addMethod(name.lexeme)) {
            error(name, "Method '" + name.lexeme + "' already defined");
        }

        analyser.push();
        for (Token param : function.params) {
            createVar(param, null, true);
        }
        resolve(function.body);
        analyser.pop();
        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        analyser.push();
        resolve(stmt.statements);
        analyser.pop();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {


        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Main.error(stmt.keyword, "Can't return from top-level code.", lines[stmt.keyword.line]);
        }

        if (stmt.value != null) {
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        if (!analyser.inLoop()) error(stmt.type, "'" + stmt.type.lexeme + "' can only be used inside loops");
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Token name = stmt.name;
        if (analyser.addVar(name.lexeme, null, stmt.initializer != null)) {
            error(name, "Variable '" + name.lexeme + "' already defined");
        }

        if (stmt.initializer != null) resolve(stmt.initializer);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        analyser.push();
        analyser.pushLoop();
        resolve(stmt.body);
        analyser.popLoop();
        analyser.pop();
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        analyser.push();
        analyser.pushLoop();

        if (stmt.init != null) resolve(stmt.init);
        if (stmt.condition != null) resolve(stmt.condition);
        if (stmt.increment != null) resolve(stmt.increment);
        resolve(stmt.body);

        analyser.popLoop();
        analyser.pop();
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        checkVarExistence(expr.name, expr.type.type != TokenType.ASSIGN);
        if (expr.type.type == TokenType.ASSIGN) define(expr.name);
        return null;
    }

    @Override
    public Void visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        checkVarExistence(expr.name, true);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null; //not really anything to do here
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        checkVarExistence(expr.name, true);

        return null;
    }

    @Override
    public Void visitFunctionExpr(Expr.Function expr) {
        Token name = expr.name;
        if (!analyser.hasMethod(name.lexeme)) error(name, "Method '" + name.lexeme + "' not defined");
        return null;
    }
}