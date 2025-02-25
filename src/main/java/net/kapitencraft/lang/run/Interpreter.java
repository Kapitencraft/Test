package net.kapitencraft.lang.run;

import net.kapitencraft.lang.exception.runtime.AbstractScriptedException;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.inst.AbstractClassInstance;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.exception.CancelBlock;
import net.kapitencraft.lang.exception.EscapeLoop;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;
import net.kapitencraft.tool.Math;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.tool.Pair;

import java.util.*;
import java.util.function.Consumer;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    public static final Interpreter INSTANCE = new Interpreter();

    private Environment environment = new Environment();
    private final CallStack callStack = new CallStack();

    public Consumer<String> output = System.out::println;

    public static final Scanner in = new Scanner(System.in);

    public static boolean suppressClassLoad = false;

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

    public void runMainMethod(ScriptedClass target, String data, boolean profiling, boolean output) {
        if (!target.hasStaticMethod("main")) return;
        suppressClassLoad = true;
        Optional.ofNullable(target.getStaticMethod("main", List.of(VarTypeManager.STRING.array())))
                .ifPresentOrElse(method -> {
                    suppressClassLoad = false;
                    this.pushCall(target.absoluteName(), "main", target.name());
                    try {
                        millisAtStart = System.currentTimeMillis();
                        this.environment.push();
                        ArrayList<Object> obj = new ArrayList<>(); //can't use List.of() because it would add each of the split strings as a single element to the list
                        obj.add(data.split(" "));
                        method.call(new Environment(), this, obj);
                        if (output) {
                            if (profiling)
                                System.out.println("\u001B[32mExecution took " + elapsedMillis() + "ms\u001B[0m");
                            else System.out.println("\u001B[32mExecution finished\u001B[0m");
                        }
                    } catch (AbstractScriptedException e) {
                        System.err.println("Caused by: " + e.exceptionType.getType().absoluteName() + ": " + e.exceptionType.getField("message"));
                        this.callStack.printStackTrace(System.err::println);
                        System.exit(65);
                    } finally {
                        this.environment.pop();
                        this.callStack.clear();
                    }
                }, () -> {
                    System.err.printf("could not find executable main method inside class '%s'", target.absoluteName());
                    suppressClassLoad = false;
                });
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
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new CancelBlock(value);
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        AbstractScriptedException exception = new AbstractScriptedException((AbstractClassInstance) evaluate(stmt.value));
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
    public Void visitForEachStmt(Stmt.ForEach stmt) {
        this.environment.push();
        for (Object obj : (Object[]) evaluate(stmt.initializer)) {
            environment.defineVar(stmt.name.lexeme(), obj);
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
            a: for (Pair<Pair<List<ClassReference>, Token>, Stmt.Block> pair : stmt.catches) {
                Pair<List<ClassReference>, Token> constData = pair.left();
                ScriptedClass causer = e.exceptionType.getType();
                for (ClassReference loxClass : constData.left()) {
                    if (causer.isChildOf(loxClass.get())) {
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
        return expr.holder.value();
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
        else value = environment.assignVarWithOperator(expr.type, expr.name, value, expr.executor.get(), expr.operand);
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
        return visitAlgebra(left, right, expr.executor.get(), expr.operator, expr.operand);
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
        if (object instanceof AbstractClassInstance instance) {
            ScriptedClass type = instance.getType();
            if (type.isParentOf(expr.targetType.get())) {
                if (expr.patternVarName != null) {
                    environment.defineVar(expr.patternVarName.lexeme(), instance);
                }
                return true;
            }
        }
        return false;
    }

    public void popCall() {
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
        AbstractClassInstance inst = (AbstractClassInstance) evaluate(expr.callee);
        pushCallIndex(expr.name.line());
        pushCall(inst.getType().absoluteName(), expr.name.lexeme(), inst.getType().name());
        Object data = inst.executeMethod(expr.name.lexeme(), expr.methodOrdinal, this.visitArgs(expr.args), this);
        popCall();
        return data;
    }

    @Override
    public Object visitStaticCallExpr(Expr.StaticCall expr) {
        pushCallIndex(expr.name.line());
        pushCall(expr.target.get().absoluteName(), expr.name.lexeme(), expr.target.get().name());
        Object data = staticCall(expr.target.get(), expr.name.lexeme(), expr.methodOrdinal, visitArgs(expr.args));
        popCall();
        return data;
    }

    private Object staticCall(ScriptedClass target, String name, int ordinal, List<Object> args) {
        return target.getStaticMethodByOrdinal(name, ordinal).call(new Environment(), this, args);
    }

    public List<Object> visitArgs(List<Expr> args) {
        return args.stream().map(this::evaluate).toList();
    }

    @Override
    public Object visitConstructorExpr(Expr.Constructor expr) {
        pushCallIndex(expr.keyword.line());
        pushCall(expr.target.get().absoluteName(), "<init>", expr.target.name());
        Object data = expr.target.get().createInst(expr.params, expr.ordinal, this);
        popCall();
        return data;
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        return ((AbstractClassInstance) evaluate(expr.object)).getField(expr.name.lexeme());
    }

    @Override
    public Object visitStaticGetExpr(Expr.StaticGet expr) {
        return expr.target.get().getStaticField(expr.name.lexeme());
    }

    @Override
    public Object visitArrayGetExpr(Expr.ArrayGet expr) {
        Object[] array = (Object[]) evaluate(expr.object);
        int index = (int) evaluate(expr.index);
        if (array.length < index || index < 0) {
            pushCallIndex(0);
            throw AbstractScriptedException.createException(VarTypeManager.INDEX_OUT_OF_BOUNDS_EXCEPTION, "index " + expr.index + " out of bounds for length " + array.length);
        }
        return array[index];
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object val = evaluate(expr.value);
        AbstractClassInstance instance = (AbstractClassInstance) evaluate(expr.object);
        if (expr.assignType.type() == TokenType.ASSIGN) {
            return instance.assignField(expr.name.lexeme(), val);
        } else {
            return instance.assignFieldWithOperator(expr.name.lexeme(), val, expr.assignType, null, null);
        }
    }

    @Override
    public Object visitStaticSetExpr(Expr.StaticSet expr) {
        Object val = evaluate(expr.value);
        if (expr.assignType.type() == TokenType.ASSIGN) {
            return expr.target.get().assignStaticField(expr.name.lexeme(), val);
        } else {
            return expr.target.get().assignStaticFieldWithOperator(expr.name.lexeme(), val, expr.assignType, null, null);
        }
    }

    public Object visitAlgebra(Object left, Object right, ScriptedClass executor, Token operator, Operand operand) {
        try {
            return executor.doOperation(OperationType.of(operator), operand, operand == Operand.LEFT ? left : right, operand == Operand.LEFT ? right : left);
        } catch (ArithmeticException e) {
            pushCallIndex(operator.line());
            throw AbstractScriptedException.createException(VarTypeManager.ARITHMETIC_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public Object visitArraySetExpr(Expr.ArraySet expr) {
        Object[] array = (Object[]) evaluate(expr.object);
        int index = (int) evaluate(expr.index);
        if (array.length < index || index < 0) {
            pushCallIndex(0);
            throw AbstractScriptedException.createException(VarTypeManager.INDEX_OUT_OF_BOUNDS_EXCEPTION, "index " + expr.index + " out of bounds for length " + array.length);
        }
        return array[index] = Interpreter.INSTANCE.visitAlgebra(array[index], evaluate(expr.value), null, expr.assignType, null);
    }

    @Override
    public Object visitSpecialSetExpr(Expr.SpecialSet expr) {
        return ((AbstractClassInstance) evaluate(expr.callee)).specialAssign(expr.name.lexeme(), expr.assignType);
    }

    @Override
    public Object visitStaticSpecialExpr(Expr.StaticSpecial expr) {
        return expr.target.get().staticSpecialAssign(expr.name.lexeme(), expr.assignType);
    }

    @Override
    public Object visitArraySpecialExpr(Expr.ArraySpecial expr) {
        Object[] array = (Object[]) evaluate(expr.object);
        int index = (int) evaluate(expr.index);
        if (array.length < index || index < 0) {
            pushCallIndex(0);
            throw AbstractScriptedException.createException(VarTypeManager.INDEX_OUT_OF_BOUNDS_EXCEPTION, "index " + expr.index + " out of bounds for length " + array.length);
        }
        return array[index] = Math.specialMerge(array[index], expr.assignType);
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

    public long elapsedMillis() {
        return System.currentTimeMillis() - millisAtStart;
    }
}
