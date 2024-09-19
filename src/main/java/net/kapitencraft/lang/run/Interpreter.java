package net.kapitencraft.lang.run;

import net.kapitencraft.lang.oop.ClassInstance;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.exception.CancelBlock;
import net.kapitencraft.lang.exception.EscapeLoop;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.func.LoxFunction;
import net.kapitencraft.lang.oop.GeneratedLoxClass;
import net.kapitencraft.lang.oop.LoxClass;
import net.kapitencraft.tool.Math;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.tool.Pair;

import java.util.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;
    public static final Scanner in = new Scanner(System.in);

    public static long millisAtStart;

    public Interpreter() {

        millisAtStart = System.currentTimeMillis();
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

    public void interpret(List<Stmt> statements, Environment active) {
        System.out.println("Executing...");
        Environment shadowed = environment;
        try {
            environment = active == null ? environment : active;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Main.runtimeError(error);
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

        environment.defineVar(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFuncDeclStmt(Stmt.FuncDecl stmt) {
        LoxFunction function = new LoxFunction(stmt);
        environment.defineMethod(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new CancelBlock(value);
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
    public Void visitImportStmt(Stmt.Import stmt) {
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        LoxClass loxClass = new GeneratedLoxClass(stmt);
        if (!loxClass.hasStaticMethod("main")) return null;
        LoxCallable callable = loxClass.getStaticMethod("main");
        if (callable.arity() == 0) {
            callable.call(new Environment(), this, List.of());
        }
        //do nothing; classes are loaded inside class loader
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (EscapeLoop escape) {
                if (escape.token.type == TokenType.BREAK) break;
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
                if (escape.token.type == TokenType.BREAK) break;
                //no need to "continue" as the JVM already does it when breaking out of the body
            }
        }
        this.environment.pop();
        return null;
    }

    //TODO ensure vars are first initialized

    @Override
    public Void visitLoopInterruptionStmt(Stmt.LoopInterruption stmt) {
        throw new EscapeLoop(stmt.type);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value.literal.value();
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitVarRefExpr(Expr.VarRef expr) {
        return environment.getVar(expr.name.lexeme);
    }

    @Override
    public Object visitFuncRefExpr(Expr.FuncRef expr) {
        return environment.getMethod(expr.name.lexeme);
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

        return switch (expr.operator.type) {
            case NOT -> !isTruthy(right);
            case SUB -> {
                checkNumberOperand(expr.operator, right);
                if (right instanceof Double) {
                    yield -(double) right;
                } else yield -(int) right;
            }
            default -> null;
        };
    }

    @Override
    public Object visitClassRefExpr(Expr.ClassRef expr) {
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        if (expr.type.type == TokenType.ASSIGN) environment.assignVar(expr.name.lexeme, value);
        else value = environment.assignVarWithOperator(expr.type, expr.name.lexeme, value);
        return value;
    }

    @Override
    public Object visitSpecialAssignExpr(Expr.SpecialAssign expr) {
        return environment.specialVarAssign(expr.name.lexeme, expr.type);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return Math.mergeGreater(left, right);
            case GEQUAL:
                checkNumberOperands(expr.operator, left, right);
                return Math.mergeGEqual(left, right);
            case LESSER:
                checkNumberOperands(expr.operator, left, right);
                return Math.mergeLesser(left, right);
            case LEQUAL:
                checkNumberOperands(expr.operator, left, right);
                return Math.mergeLEqual(left, right);
            case NEQUAL: return !isEqual(left, right);
            case EQUAL: return isEqual(left, right);
            case POW:
                checkNumberOperands(expr.operator, left, right);
                return Math.mergePow(left, right);
            case SUB:
                checkNumberOperands(expr.operator, left, right);
                return Math.mergeSub(left, right);
            case DIV:
                checkNumberOperands(expr.operator, left, right);
                return Math.mergeDiv(left, right);
            case MUL:
                checkNumberOperands(expr.operator, left, right);
                return Math.mergeMul(left, right);
            case MOD:
                checkNumberOperands(expr.operator, left, right);
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
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = visitArgs(expr.args);

        if (callee instanceof LoxCallable function) {
            if (arguments.size() != function.arity()) {
                throw new RuntimeError(expr.paren, "Expected " +
                        function.arity() + " arguments but got " +
                        arguments.size() + ".");
            }

            return function.call( this.environment, this, arguments);
        }
        throw new RuntimeError(expr.paren, "unknown function");
    }

    public List<Object> visitArgs(List<Expr> args) {
        return args.stream().map(this::evaluate).toList();
    }

    @Override
    public Object visitConstructorExpr(Expr.Constructor expr) {
        return expr.target.createInst(expr.params, this);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        return ((ClassInstance) evaluate(expr.object)).getField(expr.name.lexeme);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object val = evaluate(expr.value);
        ClassInstance instance = (ClassInstance) evaluate(expr.object);
        if (expr.assignType.type == TokenType.ASSIGN) {
            return instance.assignField(expr.name.lexeme, val);
        } else {
            return instance.assignFieldWithOperator(expr.name.lexeme, val, expr.assignType);
        }
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }


    public static void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        if (operand instanceof Integer) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    public static void checkNumberOperands(Token operator, Object left, Object right) {
        boolean leftCheck = left instanceof Double || left instanceof Integer;
        boolean rightCheck = right instanceof Double || right instanceof Integer;

        if (leftCheck && rightCheck) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    public Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }
}
