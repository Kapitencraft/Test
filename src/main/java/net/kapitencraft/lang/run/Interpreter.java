package net.kapitencraft.lang.run;

import net.kapitencraft.lang.exception.runtime.AbstractScriptedException;
import net.kapitencraft.lang.holder.ast.RuntimeExpr;
import net.kapitencraft.lang.holder.ast.RuntimeStmt;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.oop.clazz.inst.ClassInstance;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.exception.CancelBlock;
import net.kapitencraft.lang.exception.EscapeLoop;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.oop.clazz.ScriptedClass;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;
import net.kapitencraft.lang.run.natives.NativeClassInstance;
import net.kapitencraft.lang.run.natives.NativeClassLoader;
import net.kapitencraft.lang.run.natives.impl.NativeClassImpl;
import net.kapitencraft.lang.tool.Math;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.tool.Pair;

import java.util.*;
import java.util.function.Consumer;

public class Interpreter implements RuntimeExpr.Visitor<Object>, RuntimeStmt.Visitor<Void> {
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
        return object == null ? "null" : object.toString();
    }

    public void runMainMethod(ScriptedClass target, String data, boolean profiling, boolean output) {
        if (!target.hasStaticMethod("main")) return;
        suppressClassLoad = true;
        Optional.ofNullable(target.getStaticMethod("main", new ClassReference[] {VarTypeManager.STRING.array()}))
                .ifPresentOrElse(method -> {
                    suppressClassLoad = false;
                    this.pushCall(target.absoluteName(), "main", target.name());
                    try {
                        millisAtStart = System.currentTimeMillis();
                        this.environment.push();
                        ArrayList<Object> obj = new ArrayList<>(); //can't use List.of() because it would add each of the split strings as a single element to the list
                        obj.add(Arrays.stream(data.split(" ")).map(NativeClassLoader::wrapString).toArray());
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

    public void interpret(RuntimeStmt[] statements, Environment active) {
        Environment shadowed = environment;
        try {
            environment = active == null ? environment : active;
            for (RuntimeStmt statement : statements) {
                execute(statement);
            }
        } finally {
            environment = shadowed;
        }
    }

    private void execute(RuntimeStmt stmt) {
        if (stmt == null) return;
        stmt.accept(this);
    }

    @Override
    public Void visitVarDeclStmt(RuntimeStmt.VarDecl stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.defineVar(stmt.name, value);
        return null;
    }

    @Override
    public Void visitExpressionStmt(RuntimeStmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(RuntimeStmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new CancelBlock(value);
    }

    @Override
    public Void visitThrowStmt(RuntimeStmt.Throw stmt) {
        AbstractScriptedException exception = new AbstractScriptedException((ClassInstance) evaluate(stmt.value));
        pushCallIndex(stmt.line);
        throw exception;
    }

    @Override
    public Void visitIfStmt(RuntimeStmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
            return null;
        }

        for (Pair<RuntimeExpr, RuntimeStmt> elif : stmt.elifs) {
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
    public Void visitBlockStmt(RuntimeStmt.Block stmt) {
        executeBlock(stmt.statements);
        return null;
    }

    @Override
    public Void visitWhileStmt(RuntimeStmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (EscapeLoop escape) {
                if (escape.type == TokenType.BREAK) break;
                //no need to "continue" as the JVM already does it when breaking out of the body
            }
        }
        return null;
    }

    @Override
    public Void visitForStmt(RuntimeStmt.For stmt) {
        this.environment.push();
        for (execute(stmt.init); isTruthy(evaluate(stmt.condition)); evaluate(stmt.increment)) {
            try {
                execute(stmt.body);
            } catch (EscapeLoop escape) {
                if (escape.type == TokenType.BREAK) break;
                //no need to "continue" as the JVM already does it when breaking out of the body
            }
        }
        this.environment.pop();
        return null;
    }

    @Override
    public Void visitForEachStmt(RuntimeStmt.ForEach stmt) {
        this.environment.push();
        //TODO fix primitive type crash
        for (Object obj : (Object[]) evaluate(stmt.initializer)) {
            environment.defineVar(stmt.name, obj);
            try {
                execute(stmt.body);
            } catch (EscapeLoop escape) {
                if (escape.type == TokenType.BREAK) break;
                //no need to "continue" as the JVM already does it when breaking out of the body
            }
        }
        this.environment.pop();
        return null;
    }

    @Override
    public Void visitLoopInterruptionStmt(RuntimeStmt.LoopInterruption stmt) {
        throw new EscapeLoop(TokenType.CONTINUE); //stmt.type);
    }

    @Override
    public Void visitTryStmt(RuntimeStmt.Try stmt) {
        int stackIndex = this.callStack.stack.size();
        try {
            visitBlockStmt(stmt.body);
        } catch (AbstractScriptedException e) {
            this.callStack.resetToSize(stackIndex);
            boolean handled = false;
            a: for (Pair<Pair<ClassReference[], String>, RuntimeStmt.Block> pair : stmt.catches) {
                Pair<ClassReference[], String> constData = pair.left();
                ScriptedClass causer = e.exceptionType.getType();
                for (ClassReference loxClass : constData.left()) {
                    if (causer.isChildOf(loxClass.get())) {
                        environment.push();
                        environment.defineVar(constData.right(), e.exceptionType);
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
    public Object visitGroupingExpr(RuntimeExpr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(RuntimeExpr.Literal expr) {
        if (expr.literal.value() instanceof String s) {
            return NativeClassLoader.wrapString(s);
        }
        return expr.literal.value();
    }

    @Override
    public Object visitLogicalExpr(RuntimeExpr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else if (expr.operator == TokenType.XOR) {
            return isTruthy(left) ^ isTruthy(evaluate(expr.right));
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitVarRefExpr(RuntimeExpr.VarRef expr) {
        return environment.getVar(expr.name);
    }

    public void executeBlock(RuntimeStmt[] statements) {
        this.environment.push();
        try {
            for (RuntimeStmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment.pop();
        }
    }

    @Override
    public Object visitUnaryExpr(RuntimeExpr.Unary expr) {
        Object right = evaluate(expr.right);

        return switch (expr.operator) {
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
    public Object visitAssignExpr(RuntimeExpr.Assign expr) {
        Object value = evaluate(expr.value);
        if (expr.type == TokenType.ASSIGN) environment.assignVar(expr.name, value);
        else value = environment.assignVarWithOperator(expr.type, expr.line, expr.name, value, expr.executor.get(), expr.operand);
        return value;
    }

    @Override
    public Object visitSpecialAssignExpr(RuntimeExpr.SpecialAssign expr) {
        return environment.specialVarAssign(expr.name, expr.assignType);
    }

    @Override
    public Object visitBinaryExpr(RuntimeExpr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        return visitAlgebra(left, right, expr.executor.get(), expr.operator, expr.line, expr.operand);
    }

    @Override
    public Object visitWhenExpr(RuntimeExpr.When expr) {
        return isTruthy(evaluate(expr.condition)) ? evaluate(expr.ifTrue) : evaluate(expr.ifFalse);
    }

    @Override
    public Object visitSwitchExpr(RuntimeExpr.Switch stmt) {
        Object o = evaluate(stmt.provider);
        return stmt.params.containsKey(o) ? evaluate(stmt.params.get(o)) : evaluate(stmt.defaulted);
    }

    @Override
    public Object visitCastCheckExpr(RuntimeExpr.CastCheck expr) {
        Object object = evaluate(expr.object);
        if (object instanceof ClassInstance instance) {
            ScriptedClass type = instance.getType();
            if (type.isParentOf(expr.targetType.get())) {
                if (expr.patternVarName != null) {
                    environment.defineVar(expr.patternVarName, instance);
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
    public Object visitInstCallExpr(RuntimeExpr.InstCall expr) {
        ClassInstance inst = (ClassInstance) evaluate(expr.callee);
        pushCallIndex(expr.name.line());
        pushCall(inst.getType().absoluteName(), expr.name.lexeme(), inst.getType().name());
        Object data = inst.executeMethod(expr.name.lexeme(), expr.methodOrdinal, this.visitArgs(expr.args), this);
        popCall();
        return data;
    }

    @Override
    public Object visitStaticCallExpr(RuntimeExpr.StaticCall expr) {
        pushCallIndex(expr.name.line());
        pushCall(expr.target.get().absoluteName(), expr.name.lexeme(), expr.target.get().name());
        Object data = staticCall(expr.target.get(), expr.name.lexeme(), expr.methodOrdinal, visitArgs(expr.args));
        popCall();
        return data;
    }

    private Object staticCall(ScriptedClass target, String name, int ordinal, List<Object> args) {
        return target.getStaticMethodByOrdinal(name, ordinal).call(new Environment(), this, args);
    }

    public List<Object> visitArgs(RuntimeExpr[] args) {
        return Arrays.stream(args).map(this::evaluate).toList();
    }

    @Override
    public Object visitConstructorExpr(RuntimeExpr.Constructor expr) {
        pushCallIndex(expr.line);
        pushCall(expr.target.get().absoluteName(), "<init>", expr.target.name());
        Object data = expr.target.get().createInst(expr.params, expr.ordinal, this);
        popCall();
        return data;
    }

    @Override
    public Object visitGetExpr(RuntimeExpr.Get expr) {
        return ((ClassInstance) evaluate(expr.object)).getField(expr.name);
    }

    @Override
    public Object visitStaticGetExpr(RuntimeExpr.StaticGet expr) {
        return expr.target.get().getStaticField(expr.name);
    }

    @Override
    public Object visitArrayGetExpr(RuntimeExpr.ArrayGet expr) {
        Object[] array = (Object[]) evaluate(expr.object);
        int index = (int) evaluate(expr.index);
        if (array.length < index || index < 0) {
            pushCallIndex(0);
            throw AbstractScriptedException.createException(VarTypeManager.INDEX_OUT_OF_BOUNDS_EXCEPTION, "index " + expr.index + " out of bounds for length " + array.length);
        }
        return array[index];
    }

    @Override
    public Object visitSetExpr(RuntimeExpr.Set expr) {
        Object val = evaluate(expr.value);
        ClassInstance instance = (ClassInstance) evaluate(expr.object);
        if (expr.assignType == TokenType.ASSIGN) {
            return instance.assignField(expr.name, val);
        } else {
            return instance.assignFieldWithOperator(expr.name, val, expr.assignType, expr.line, null, null);
        }
    }

    @Override
    public Object visitStaticSetExpr(RuntimeExpr.StaticSet expr) {
        Object val = evaluate(expr.value);
        if (expr.assignType == TokenType.ASSIGN) {
            return expr.target.get().assignStaticField(expr.name, val);
        } else {
            return expr.target.get().assignStaticFieldWithOperator(expr.name, val, expr.assignType, expr.line, null, null);
        }
    }

    public Object visitAlgebra(Object left, Object right, ScriptedClass executor, TokenType operator, int line, Operand operand) {
        try {
            return executor.doOperation(OperationType.of(operator), operand, operand == Operand.LEFT ? left : right, operand == Operand.LEFT ? right : left);
        } catch (ArithmeticException e) {
            pushCallIndex(line);
            throw AbstractScriptedException.createException(VarTypeManager.ARITHMETIC_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public Object visitArraySetExpr(RuntimeExpr.ArraySet expr) {
        Object[] array = (Object[]) evaluate(expr.object);
        int index = (int) evaluate(expr.index);
        if (array.length < index || index < 0) {
            pushCallIndex(0);
            throw AbstractScriptedException.createException(VarTypeManager.INDEX_OUT_OF_BOUNDS_EXCEPTION, "index " + expr.index + " out of bounds for length " + array.length);
        }
        return array[index] = Interpreter.INSTANCE.visitAlgebra(array[index], evaluate(expr.value), null, expr.assignType, expr.line, null);
    }

    @Override
    public Object visitSpecialSetExpr(RuntimeExpr.SpecialSet expr) {
        return ((ClassInstance) evaluate(expr.callee)).specialAssign(expr.name, expr.assignType);
    }

    @Override
    public Object visitStaticSpecialExpr(RuntimeExpr.StaticSpecial expr) {
        return expr.target.get().staticSpecialAssign(expr.name, expr.assignType);
    }

    @Override
    public Object visitArraySpecialExpr(RuntimeExpr.ArraySpecial expr) {
        Object[] array = (Object[]) evaluate(expr.object);
        int index = (int) evaluate(expr.index);
        if (array.length < index || index < 0) {
            pushCallIndex(0);
            throw AbstractScriptedException.createException(VarTypeManager.INDEX_OUT_OF_BOUNDS_EXCEPTION, "index " + expr.index + " out of bounds for length " + array.length);
        }
        return array[index] = Math.specialMerge(array[index], expr.assignType);
    }

    @Override
    public Object visitSliceExpr(RuntimeExpr.Slice expr) {
        Object[] array = (Object[]) evaluate(expr.object);
        int interval = expr.interval != null ? (int) evaluate(expr.interval) : 1;
        int min = expr.start != null ? (int) evaluate(expr.start) : interval < 0 ? array.length : 0;
        int max = expr.end != null ? (int) evaluate(expr.end) : interval < 0 ? 0 : array.length;
        Object[] out = new Object[(max - min) / interval];
        int index = 0;
        for (int i = min; i < max; i+=interval) {
            out[index] = array[i];
            index++;
        }
        return out;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    public Object evaluate(RuntimeExpr expr) {
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
