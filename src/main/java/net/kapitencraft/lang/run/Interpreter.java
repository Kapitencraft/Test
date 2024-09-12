package net.kapitencraft.lang.run;

import net.kapitencraft.lang.ast.Expr;
import net.kapitencraft.lang.ast.Stmt;
import net.kapitencraft.lang.ast.token.Token;
import net.kapitencraft.lang.ast.token.TokenType;
import net.kapitencraft.lang.exception.CancelBlock;
import net.kapitencraft.lang.exception.EscapeLoop;
import net.kapitencraft.lang.env.core.Environment;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.func.LoxFunction;
import net.kapitencraft.tool.Math;

import java.util.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private final Environment environment = globals;
    private final Scanner in = new Scanner(System.in);

    private final long millisAtStart;

    public Interpreter() {
        globals.defineMethod("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Class<?> type() {
                return Integer.class;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) (System.currentTimeMillis() - millisAtStart);
            }

            @Override
            public String toString() {
                return "<native fn#clock>";
            }
        });
        globals.defineMethod("print", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Class<?> type() {
                return Void.class;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.out.println(stringify(arguments.get(0)));
                return null;
            }
        });
        globals.defineMethod("randInt", new LoxCallable() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Class<?> type() {
                return Integer.class;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Random random = new Random();
                int min = (int) arguments.get(0);
                int max = (int) arguments.get(1);
                return random.nextInt((max - min) + 1) + min;
            }
        });
        globals.defineMethod("abs", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Class<?> type() {
                return Integer.class;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return java.lang.Math.abs((int) arguments.get(0));
            }
        });
        globals.defineMethod("input", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Class<?> type() {
                return String.class;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.out.print(stringify(arguments.get(0)));
                return in.nextLine();
            }
        });
        millisAtStart = System.currentTimeMillis();
    }


    private String stringify(Object object) {
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

    void interpret(List<Stmt> statements) {
        System.out.println("Executing...");
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Main.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
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
    public Void visitFunctionStmt(Stmt.Function stmt) {
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
        } else if (stmt.elseBranch != null) {
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
        return expr.value;
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
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.getVar(expr.name.lexeme);
    }

    @Override
    public Object visitFunctionExpr(Expr.Function expr) {
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
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        if (expr.type.type == TokenType.ASSIGN) environment.assignVar(expr.name.lexeme, value);
        else value = environment.assignVarWithOperator(expr.type, expr.name.lexeme, value);
        return value;
    }

    @Override
    public Object visitSpecialAssignExpr(Expr.SpecialAssign expr) {

        return environment.specialVarAssign(expr.name.lexeme, expr.type.type);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GEQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer lI && right instanceof Integer rI) return lI >= rI;
                if (left instanceof Double lD && right instanceof Double rD) return lD >= rD;
                if (left instanceof Integer lI && right instanceof Double rD) return lI >= rD;
                if (left instanceof Double lD && right instanceof Integer rI) return lD >= rI;
                throw new RuntimeError(expr.operator, "Unknown number");
            case LESSER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LEQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case NEQUAL: return !isEqual(left, right);
            case EQUAL: return isEqual(left, right);
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
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (callee instanceof LoxCallable function) {
            if (arguments.size() != function.arity()) {
                throw new RuntimeError(expr.args, "Expected " +
                        function.arity() + " arguments but got " +
                        arguments.size() + ".");
            }

            return function.call(this, arguments);
        }
        throw new RuntimeError(expr.args, "unknown function");
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

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }
}
