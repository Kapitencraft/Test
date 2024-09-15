package net.kapitencraft.lang.compile.analyser;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.compile.Compiler;
import net.kapitencraft.lang.compile.LocationFinder;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.func.LoxFunction;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.token.TokenTypeCategory;
import net.kapitencraft.lang.run.Main;
import net.kapitencraft.tool.Pair;
import net.kapitencraft.lang.holder.ast.Stmt;

import java.util.List;

public class Resolver implements Expr.Visitor<Class<?>>, Stmt.Visitor<Void> {

    private final EnvAnalyser analyser = new EnvAnalyser();
    private final LocationFinder finder = new LocationFinder();
    private FunctionType currentFunction = FunctionType.NONE;
    private Class<?> funcRetType;
    private Class<?> paramType;
    private final Compiler.ErrorConsumer errorConsumer;
    private final String[] lines;

    public Resolver(Compiler.ErrorConsumer errorConsumer, String[] lines) {
        this.errorConsumer = errorConsumer;
        this.lines = lines;

        Main.natives.forEach(analyser::addMethod);
    }

    private void error(Token token, String message) {
        errorConsumer.error(token, message, lines[token.line - 1]);
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

    private Class<?> resolveCondition(Expr condition) {
        return resolve(finder.find(condition), condition, Boolean.class);
    }

    private Class<?> resolve(Token errorLoc, Expr expr, Class<?> expected) {
        Class<?> got = resolve(expr);
        if (expected == Object.class) return got;
        if (expected == Number.class && (got == Integer.class || got == Float.class || got == Double.class)) return got;
        if (got != expected) error(errorLoc, "incompatible types: " + got.getName() + " cannot be converted to " + expected.getCanonicalName());
        return got;
    }

    private void resolveFunction(Stmt.FuncDecl function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        Token name = function.name;
        if (analyser.addMethod(name.lexeme, new LoxFunction(function))) {
            error(name, "Method '" + name.lexeme + "' already defined");
        }
        funcRetType = VarTypeManager.getClassForName(function.retType.lexeme);

        analyser.push();
        for (Pair<Token, Token> pair : function.params) {
            createVar(pair.right(), null, true);
        }
        resolve(function.body);
        analyser.pop();
        currentFunction = enclosingFunction;
        funcRetType = Void.class;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        analyser.push();
        resolve(stmt.statements);
        analyser.pop();
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFuncDeclStmt(Stmt.FuncDecl stmt) {
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolveCondition(stmt.condition);
        resolve(stmt.thenBranch);
        stmt.elifs.forEach((pair) -> {
            resolveCondition(pair.left());
            resolve(pair.right());
        });
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            error(stmt.keyword, "Can't return from top-level code.");
        }


        if (stmt.value != null) {
            resolve(finder.find(stmt.value), stmt.value, funcRetType);
        } else if (funcRetType != Void.class) {
            error(stmt.keyword, "incompatible types: unexpected return value.");
        }

        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        if (!analyser.inLoop()) error(stmt.type, "'" + stmt.type.lexeme + "' can only be used inside loops");
        return null;
    }

    @Override
    public Void visitVarDeclStmt(Stmt.VarDecl stmt) {
        Token name = stmt.name;
        if (analyser.addVar(name.lexeme, stmt.type.lexeme, stmt.initializer != null)) {
            error(name, "Variable '" + name.lexeme + "' already defined");
        }


        if (stmt.initializer != null) {
            checkVarType(name, stmt.initializer);
            resolve(stmt.initializer);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolveCondition(stmt.condition);
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
        if (type.categories.contains(TokenTypeCategory.COMPARATORS)) return Boolean.class;
        return left;
    }

    @Override
    public Class<?> visitWhenExpr(Expr.When expr) {
        resolveCondition(expr.condition);
        Class<?> left = resolve(expr.ifTrue);
        Class<?> right = resolve(expr.ifFalse);
        if (left != right) error(finder.find(expr.ifFalse), "When does not return the same type on both sides");
        return left;
    }

    @Override
    public Class<?> visitCallExpr(Expr.Call expr) {

        resolve(expr.callee);

        try {
            Expr.FuncRef ref = (Expr.FuncRef) expr.callee;
            LoxCallable method = analyser.getMethod(ref.name.lexeme);
            List<? extends Class<?>> params = method.argTypes();
            for (int i = 0; i < method.arity(); i++) {
                Expr arg = expr.args.get(i);
                resolve(finder.find(arg), arg, params.get(i));
            }
            return method.type();
        } catch (Exception e) {
            error(finder.find(expr.callee), "Unable to find cast method: " + e.getMessage());
        }

        return Void.class;
    }

    @Override
    public Class<?> visitGroupingExpr(Expr.Grouping expr) {
        return resolve(expr.expression);
    }

    @Override
    public Class<?> visitLiteralExpr(Expr.Literal expr) {
        return expr.value.literal.getClass();
    }

    @Override
    public Class<?> visitLogicalExpr(Expr.Logical expr) {
        resolveCondition(expr.left);
        resolveCondition(expr.right);
        return Boolean.class;
    }

    @Override
    public Class<?> visitUnaryExpr(Expr.Unary expr) {
        return expr.operator.type == TokenType.NOT ? resolveCondition(expr.right) : resolve(finder.find(expr.right), expr.right, Number.class);
    }

    @Override
    public Class<?> visitVarRefExpr(Expr.VarRef expr) {
        checkVarExistence(expr.name, true);

        return analyser.getVarType(expr.name.lexeme);
    }

    @Override
    public Class<?> visitFuncRefExpr(Expr.FuncRef expr) {
        Token name = expr.name;
        if (!analyser.hasMethod(name.lexeme)) error(name, "Method '" + name.lexeme + "' not defined");
        return analyser.getMethodType(name.lexeme);
    }

    @Override
    public Class<?> visitSwitchExpr(Expr.Switch stmt) {
        return null;
    }

}