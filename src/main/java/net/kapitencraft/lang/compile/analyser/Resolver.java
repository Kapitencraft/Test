package net.kapitencraft.lang.compile.analyser;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.ast.Expr;
import net.kapitencraft.lang.ast.Stmt;
import net.kapitencraft.lang.ast.token.Token;
import net.kapitencraft.lang.ast.token.TokenType;
import net.kapitencraft.lang.ast.token.TokenTypeCategory;
import net.kapitencraft.lang.run.Main;
import net.kapitencraft.tool.Pair;

import java.util.List;

public class Resolver implements Expr.Visitor<Class<?>>, Stmt.Visitor<Void> {

    private final EnvAnalyser analyser = new EnvAnalyser();
    private FunctionType currentFunction = FunctionType.NONE;
    private final String[] lines;

    public Resolver(String[] lines) {
        this.lines = lines;
        analyser.addMethod("clock", Integer.class);
        analyser.addMethod("print", Void.class);
        analyser.addMethod("input", String.class);
        analyser.addMethod("abs", Integer.class);
        analyser.addMethod("randInt", Integer.class);
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

    private void checkVarType(Token name, Expr value) {
        resolve(name, value, analyser.getVarType(name.lexeme));
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

    private Class<?> resolve(Expr expr) {
        return expr.accept(this);
    }

    private void resolve(Token errorLoc, Expr expr, Class<?> expected) {
        Class<?> got = resolve(expr);
        if (got != expected) error(errorLoc, "Expected " + expected + " but got " + got);
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        Token name = function.name;
        if (analyser.addMethod(name.lexeme, VarTypeManager.getClassForName(function.retType.lexeme))) {
            error(name, "Method '" + name.lexeme + "' already defined");
        }

        analyser.push();
        for (Pair<Token, Token> pair : function.params) {
            createVar(pair.right(), null, true);
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
        resolve(null, stmt.condition, Boolean.class);
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
        if (analyser.addVar(name.lexeme, stmt.type.lexeme, stmt.initializer != null)) {
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
    public Class<?> visitAssignExpr(Expr.Assign expr) {
        checkVarExistence(expr.name, expr.type.type != TokenType.ASSIGN);
        checkVarType(expr.name, expr.value);
        if (expr.type.type == TokenType.ASSIGN) define(expr.name);
        return analyser.getVarType(expr.name.lexeme);
    }

    @Override
    public Class<?> visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        checkVarExistence(expr.name, true);
        return analyser.getVarType(expr.name.lexeme);
    }

    @Override
    public Class<?> visitBinaryExpr(Expr.Binary expr) {
        Class<?> left = resolve(expr.left);
        Class<?> right = resolve(expr.right);
        TokenType type = expr.operator.type;
        if (type == TokenType.ADD && (left == String.class || right == String.class)) return left; //check if at least one of the values is string
        if (type.categories.contains(TokenTypeCategory.BOOL_BINARY) && !(left == Boolean.class && right == Boolean.class))
            error(expr.operator, "both values must be boolean");
        if (type.categories.contains(TokenTypeCategory.ARITHMETIC_BINARY) && !(left.getSuperclass() == Number.class && right.getSuperclass() == Number.class))
            error(expr.operator, "both values must be numbers");
        if (left != right)
            error(expr.operator, "can not combine values of different types");
        return left;
    }

    @Override
    public Class<?> visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }

        return analyser.getMethodType(((Expr.Function) expr.callee).name.lexeme);
    }

    @Override
    public Class<?> visitGroupingExpr(Expr.Grouping expr) {
        return resolve(expr.expression);
    }

    @Override
    public Class<?> visitLiteralExpr(Expr.Literal expr) {
        return expr.value.getClass();
    }

    @Override
    public Class<?> visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return Boolean.class;
    }

    @Override
    public Class<?> visitUnaryExpr(Expr.Unary expr) {
        return resolve(expr.right);
    }

    @Override
    public Class<?> visitVariableExpr(Expr.Variable expr) {
        checkVarExistence(expr.name, true);

        return analyser.getVarType(expr.name.lexeme);
    }

    @Override
    public Class<?> visitFunctionExpr(Expr.Function expr) {
        Token name = expr.name;
        if (!analyser.hasMethod(name.lexeme)) error(name, "Method '" + name.lexeme + "' not defined");
        return analyser.getMethodType(name.lexeme);
    }
}