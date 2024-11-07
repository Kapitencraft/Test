package net.kapitencraft.lang.run;

import net.kapitencraft.lang.exception.runtime.AbstractScriptedException;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.exception.CancelBlock;
import net.kapitencraft.lang.exception.EscapeLoop;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.tool.Math;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.tool.Pair;

import java.util.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    public static final Interpreter INSTANCE = new Interpreter();

    private Environment environment = new Environment();
    private final CallStack callStack = new CallStack();

    public static final Scanner in = new Scanner(System.in);

    public static long millisAtStart;

    public Interpreter() {
    }

    public static String stringify(Object object) {
        if (object == null) return "null";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    public void runMainMethod(LoxClass target, String data) {
        if (!target.hasStaticMethod("main")) return;
        Optional.ofNullable(target.getStaticMethod("main", List.of(VarTypeManager.STRING)))
                .ifPresentOrElse(method -> {
                    this.pushCall(target.absoluteName(), "main", target.name());
                    try {
                        millisAtStart = System.currentTimeMillis();
                        this.environment.push();
                        method.call(new Environment(), this, List.of(data));
                        System.out.println("\u001B[32mExecution finished\u001B[0m");
                    } catch (AbstractScriptedException e) {
                        System.err.println("Caused by: " + e.exceptionType.getType().absoluteName() + ": " + e.exceptionType.getField("message"));
                        this.callStack.printStackTrace(System.err::println);
                        System.exit(65);
                    } finally {
                        this.environment.pop();
                        this.callStack.clear();
                    }
                }, () -> System.err.printf("could not find executable main method inside class '%s'", target.absoluteName()));
    }

    public void interpret(List<Stmt> statements, Environment active) {
        Environment shadowed = environment;
        try {
            environment = active == null ? environment : active;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            environment = shadowed;
        }
    }

    private void execute(Stmt stmt) {
        if (stmt == null) return;
        stmt.accept(this);
    }

    @Override
    public Void visitVarDeclStmt(Stmt.VarDecl stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.defineVar(stmt.name.lexeme(), value);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFuncDeclStmt(Stmt.FuncDecl stmt) {
        return null; //TODO move field & function decl away from "stmt"
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new CancelBlock(value);
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        AbstractScriptedException exception = new AbstractScriptedException((ClassInstance) evaluate(stmt.value));
        pushCallIndex(stmt.keyword.line());
        throw exception;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
            return null;
        }

        for (Pair<Expr, Stmt> elif : stmt.elifs) {
            if (isTruthy(evaluate(elif.left()))) {
                execute(elif.right());
                return null;
            }
        }

        if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (EscapeLoop escape) {
                if (escape.token.type() == TokenType.BREAK) break;
                //no need to "continue" as the JVM already does it when breaking out of the body
            }
        }
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        this.environment.push();
        for (execute(stmt.init); isTruthy(evaluate(stmt.condition)); evaluate(stmt.increment)) {
            try {
                execute(stmt.body);
            } catch (EscapeLoop escape) {
                if (escape.token.type() == TokenType.BREAK) break;
                //no need to "continue" as the JVM already does it when breaking out of the body
            }
        }
        this.environment.pop();
        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        throw new EscapeLoop(stmt.type);
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        int stackIndex = this.callStack.stack.size();
        try {
            visitBlockStmt(stmt.body);
        } catch (AbstractScriptedException e) {
            this.callStack.resetToSize(stackIndex);
            boolean handled = false;
            a: for (Pair<Pair<List<LoxClass>, Token>, Stmt.Block> pair : stmt.catches) {
                Pair<List<LoxClass>, Token> constData = pair.left();
                LoxClass causer = e.exceptionType.getType();
                for (LoxClass loxClass : constData.left()) {
                    if (causer.isChildOf(loxClass)) {
                        environment.push();
                        environment.defineVar(constData.right().lexeme(), e.exceptionType);
                        visitBlockStmt(pair.right());
                        environment.pop();
                        handled = true;
                        break a;
                    }
                }
            }
            if (stmt.finale != null) visitBlockStmt(stmt.finale);
            if (!handled) throw e;
        }
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value.literal().value();
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type() == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else if (expr.operator.type() == TokenType.XOR) {
            return isTruthy(left) ^ isTruthy(evaluate(expr.right));
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitVarRefExpr(Expr.VarRef expr) {
        return environment.getVar(expr.name);
    }

    public void executeBlock(List<Stmt> statements) {
        this.environment.push();
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment.pop();
        }
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        return switch (expr.operator.type()) {
            case NOT -> !isTruthy(right);
            case SUB -> {
                if (right instanceof Double)
                    yield -(double) right;
                else if (right instanceof Float)
                    yield -(float) right;
                else
                    yield -(int) right;
            }
            default -> null;
        };
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        if (expr.type.type() == TokenType.ASSIGN) environment.assignVar(expr.name, value);
        else value = environment.assignVarWithOperator(expr.type, expr.name, value);
        return value;
    }

    @Override
    public Object visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        return environment.specialVarAssign(expr.name, expr.assignType);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        try {
            switch (expr.operator.type()) {
                case GREATER:
                    return Math.mergeGreater(left, right);
                case GEQUAL:
                    return Math.mergeGEqual(left, right);
                case LESSER:
                    return Math.mergeLesser(left, right);
                case LEQUAL:
                    return Math.mergeLEqual(left, right);
                case NEQUAL:
                    return !isEqual(left, right);
                case EQUAL:
                    return isEqual(left, right);
                case POW:
                    return Math.mergePow(left, right);
                case SUB:
                    return Math.mergeSub(left, right);
                case DIV:
                    return Math.mergeDiv(left, right);
                case MUL:
                    return Math.mergeMul(left, right);
                case MOD:
                    return Math.mergeMod(left, right);
                case ADD:

                    if (left instanceof String lS) {
                        return lS + stringify(right);
                    }
                    if (right instanceof String rS) {
                        return stringify(left) + rS;
                    }

                    return Math.mergeAdd(left, right);
            }
        } catch (ArithmeticException e) {
            pushCallIndex(expr.operator.line());
            throw AbstractScriptedException.createException(VarTypeManager.ARITHMETIC_EXCEPTION, e.getMessage());
        }
        return null;
    }

    @Override
    public Object visitWhenExpr(Expr.When expr) {
        return isTruthy(evaluate(expr.condition)) ? evaluate(expr.ifTrue) : evaluate(expr.ifFalse);
    }

    @Override
    public Object visitSwitchExpr(Expr.Switch stmt) {
        Object o = evaluate(stmt.provider);
        return stmt.params.containsKey(o) ? evaluate(stmt.params.get(o)) : evaluate(stmt.defaulted);
    }

    @Override
    public Object visitCastCheckExpr(Expr.CastCheck expr) {
        Object object = evaluate(expr.object);
        if (object instanceof ClassInstance instance) {
            LoxClass type = instance.getType();
            if (type.isParentOf(expr.targetType)) {
                if (expr.patternVarName != null) {
                    environment.defineVar(expr.patternVarName.lexeme(), instance);
                }
                return true;
            }
        }
        return false;
    }

    private void popCall() {
        this.callStack.pop();
    }

    public void pushCall(String classFullName, String methodName, String className) {
        this.callStack.push(classFullName + "." + methodName, className);
    }

    public void pushCallIndex(int line) {
        this.callStack.pushLineIndex(line);
    }


    @Override
    public Object visitInstCallExpr(Expr.InstCall expr) {
        ClassInstance inst = (ClassInstance) evaluate(expr.callee);
        pushCallIndex(expr.name.line());
        pushCall(inst.getType().absoluteName(), expr.name.lexeme(), inst.getType().name());
        Object data = inst.executeMethod(expr.name.lexeme(), expr.methodOrdinal, this.visitArgs(expr.args), this);
        popCall();
        return data;
    }

    @Override
    public Object visitStaticCallExpr(Expr.StaticCall expr) {
        pushCallIndex(expr.name.line());
        pushCall(expr.target.absoluteName(), expr.name.lexeme(), expr.target.name());
        Object data = staticCall(expr.target, expr.name.lexeme(), expr.methodOrdinal, visitArgs(expr.args));
        popCall();
        return data;
    }

    private Object staticCall(LoxClass target, String name, int ordinal, List<Object> args) {
        return target.getStaticMethodByOrdinal(name, ordinal).call(new Environment(), this, args);
    }

    public List<Object> visitArgs(List<Expr> args) {
        return args.stream().map(this::evaluate).toList();
    }

    @Override
    public Object visitConstructorExpr(Expr.Constructor expr) {
        pushCallIndex(expr.keyword.line());
        pushCall(expr.target.absoluteName(), "<init>", expr.target.name());
        Object data = expr.target.createInst(expr.params, expr.ordinal, this);
        popCall();
        return data;
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        return ((ClassInstance) evaluate(expr.object)).getField(expr.name.lexeme());
    }

    @Override
    public Object visitStaticGetExpr(Expr.StaticGet expr) {
        return expr.target.getStaticField(expr.name.lexeme());
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object val = evaluate(expr.value);
        ClassInstance instance = (ClassInstance) evaluate(expr.object);
        if (expr.assignType.type() == TokenType.ASSIGN) {
            return instance.assignField(expr.name.lexeme(), val);
        } else {
            return instance.assignFieldWithOperator(expr.name.lexeme(), val, expr.assignType);
        }
    }

    @Override
    public Object visitStaticSetExpr(Expr.StaticSet expr) {
        Object val = evaluate(expr.value);
        if (expr.assignType.type() == TokenType.ASSIGN) {
            return expr.target.assignStaticField(expr.name.lexeme(), val);
        } else {
            return expr.target.assignStaticFieldWithOperator(expr.name.lexeme(), val, expr.assignType);
        }
    }

    @Override
    public Object visitSpecialSetExpr(Expr.SpecialSet expr) {
        return ((ClassInstance) evaluate(expr.callee)).specialAssign(expr.name.lexeme(), expr.assignType);
    }

    @Override
    public Object visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        return expr.target.staticSpecialAssign(expr.name.lexeme(), expr.assignType);
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    public Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    public static boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }
}
