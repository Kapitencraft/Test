package net.kapitencraft.lang.compiler.parser;

import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.func.ScriptedCallable;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.LoxClass;
import net.kapitencraft.lang.run.algebra.Operand;
import net.kapitencraft.lang.run.algebra.OperationType;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.kapitencraft.lang.holder.token.TokenType.*;
import static net.kapitencraft.lang.holder.token.TokenTypeCategory.*;

@SuppressWarnings("ThrowableNotThrown")
public class ExprParser extends AbstractParser {

    public ExprParser(Compiler.ErrorLogger errorLogger) {
        super(errorLogger);
    }

    public Expr expression() {
        if (match(SWITCH)) {
            return switchExpr();
        }

        return when();
    }

    private Expr when() {
        Expr expr = castCheck();
        if (match(QUESTION_MARK)) {
            expectCondition(expr);
            Expr ifTrue = expression();
            consume(TokenType.COLON, "':' expected");
            Expr ifFalse = expression();
            LoxClass ifTrueClass = finder.findRetType(ifTrue);
            LoxClass ifFalseClass = finder.findRetType(ifFalse);
            if (!(ifTrueClass.isParentOf(ifFalseClass) || ifFalseClass.isParentOf(ifTrueClass))) error(locFinder.find(ifTrue), "both expressions on when statement must return the same type");
            expr = new Expr.When(expr, ifTrue, ifFalse);
        }

        return expr;
    }

    private Expr castCheck() {
        Expr expr = assignment();
        if (match(INSTANCEOF)) {
            LoxClass loxClass = consumeVarType();
            Token patternVar = null;
            if (match(IDENTIFIER)) {
                patternVar = previous();
                varAnalyser.add(patternVar.lexeme(), loxClass, true, false);
            }
            return new Expr.CastCheck(expr, loxClass, patternVar);
        }

        return expr;
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(ASSIGN) || match(OPERATION_ASSIGN)) {
            Token assign = previous();
            Expr value = assignment();

            if (expr instanceof Expr.VarRef variable) {
                Token name = variable.name;

                checkVarExistence(name, assign.type() != TokenType.ASSIGN,
                        false);
                checkVarType(name, value);
                Pair<LoxClass, Operand> executor;
                if (assign.type() == TokenType.ASSIGN) {
                    varAnalyser.setHasValue(name.lexeme());
                    executor = Pair.of(VarTypeManager.VOID, Operand.LEFT);
                } else executor = getExecutor(varAnalyser.getType(name.lexeme()), assign, value);

                return new Expr.Assign(name, value, assign, executor.left(), executor.right());
            } else if (expr instanceof Expr.Get get) {
                LoxClass target = finder.findRetType(get.object);
                expectType(get.name, target.getFieldType(get.name.lexeme()), finder.findRetType(value));

                Pair<LoxClass, Operand> executor;
                if (assign.type() != ASSIGN) executor = getExecutor(target, assign, value);
                else executor = Pair.of(VarTypeManager.VOID, Operand.LEFT);
                return new Expr.Set(get.object, get.name, value, assign, executor.left(), executor.right());
            } else if (expr instanceof Expr.ArrayGet get) {

                Pair<LoxClass, Operand> executor = getExecutor(get, assign, value);
                return new Expr.ArraySet(get.object, get.index, value, assign, executor.left(), executor.right());
            }

            error(assign, "Invalid assignment target.");
        }

        if (match(GROW, SHRINK)) {

            Token assign = previous();

            if (expr instanceof Expr.VarRef ref) {
                Token name = ref.name;

                checkVarExistence(name, true, false);
                return new Expr.SpecialAssign(name, assign);
            }

            if (expr instanceof Expr.Get get) {
                return new Expr.SpecialSet(get.object, get.name, assign);
            }

        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expectCondition(expr);
            expectCondition(right);
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND, XOR)) {
            Token operator = previous();
            Expr right = equality();
            expectCondition(expr);
            expectCondition(right);
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Pair<LoxClass, Operand> getExecutor(LoxClass left, Token operator, LoxClass right) {
        Operand operand = Operand.LEFT;
        LoxClass executor = left;
        OperationType type = OperationType.of(operator);
        assert type != null;
        LoxClass result = left.checkOperation(type, operand, right);
        if (result == VarTypeManager.VOID) {
            operand = Operand.RIGHT;
            result = right.checkOperation(type, operand, left);
            executor = right;
        }
        if (result == VarTypeManager.VOID) {
            error(operator, "operator '" + operator.lexeme() + "' not possible for argument types " + left.absoluteName() + " and " + right.absoluteName());
            return Pair.of(VarTypeManager.VOID, Operand.LEFT);
        }
        return Pair.of(executor, operand);
    }

    private Pair<LoxClass, Operand> getExecutor(Expr leftArg, Token operator, Expr rightArg) {
        return getExecutor(finder.findRetType(leftArg), operator, finder.findRetType(rightArg));
    }

    private Pair<LoxClass, Operand> getExecutor(LoxClass left, Token operator, Expr rightArg) {
        return getExecutor(left, operator, finder.findRetType(rightArg));
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(EQUALITY)) {
            Token operator = previous();
            Expr right = comparison();
            Pair<LoxClass, Operand> executorInfo = getExecutor(expr, operator, right);
            expr = new Expr.Binary(expr, operator, executorInfo.left(), executorInfo.right(), right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(COMPARATORS)) {
            Token operator = previous();
            Expr right = term();
            Pair<LoxClass, Operand> executorInfo = getExecutor(expr, operator, right);
            expr = new Expr.Binary(expr, operator, executorInfo.left(), executorInfo.right(), right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(SUB, ADD)) {
            Token operator = previous();
            Expr right = factor();

            Pair<LoxClass, Operand> executorInfo = getExecutor(expr, operator, right);
            expr = new Expr.Binary(expr, operator, executorInfo.left(), executorInfo.right(), right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(DIV, MUL, MOD, POW)) {
            Token operator = previous();
            Expr right = unary();
            Pair<LoxClass, Operand> executorInfo = getExecutor(expr, operator, right);
            expr = new Expr.Binary(expr, operator, executorInfo.left(), executorInfo.right(), right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(NOT, SUB)) {
            Token operator = previous();
            Expr right = unary();
            if (operator.type() == NOT) expectCondition(right);
            else expectType(right, VarTypeManager.NUMBER);
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr switchExpr() {
        Token keyword = previous();
        consumeBracketOpen("switch");

        expectType(VarTypeManager.ENUM.get(), VarTypeManager.STRING.get(), VarTypeManager.INTEGER, VarTypeManager.DOUBLE, VarTypeManager.FLOAT, VarTypeManager.CHAR);
        Expr provider = expression();
        popExpectation();

        consumeBracketClose("switch");

        consumeCurlyOpen("switch body");
        Map<Object, Expr> params = new HashMap<>();
        Expr def = null;

        while (!check(C_BRACKET_C)) {
            if (match(CASE)) {
                Object key = literal();
                if (params.containsKey(key)) error(previous(), "Duplicate case key '" + previous().lexeme() + "'");
                consume(LAMBDA, "not a statement");
                Expr expr = expression();
                consumeEndOfArg();
                params.put(key, expr);
            } else if (match(DEFAULT)) {
                if (def != null) error(previous(), "Duplicate default key");
                consume(LAMBDA, "not a statement");
                def = expression();
                consumeEndOfArg();
            } else {
                error(peek(), "unexpected token");
            }
        }

        consumeCurlyClose("switch body");
        return new Expr.Switch(provider, params, def, keyword);
    }

    private Expr finishInstCall(Expr.Get get) {
        List<Expr> arguments = args();

        List<LoxClass> givenTypes = arguments.stream().map(this.finder::findRetType).toList();
        LoxClass targetClass = this.finder.findRetType(get.object);

        int ordinal = targetClass.getMethodOrdinal(get.name.lexeme(), givenTypes);
        if (ordinal == -1) {
            error(get.name, "unknown symbol");
            consumeBracketClose("arguments");
            return new Expr.InstCall(get.object, get.name, ordinal, arguments);
        }
        ScriptedCallable callable = targetClass.getMethodByOrdinal(get.name.lexeme(), ordinal);

        checkArguments(arguments, callable, get.name);

        consumeBracketClose("arguments");

        return new Expr.InstCall(get.object, get.name, ordinal, arguments);
    }

    private Expr statics() {
        LoxClass target = consumeVarType();
        Token name = previous();
        if (check(BRACKET_O)) return staticCall(target, name);
        if (match(ASSIGN) || match(OPERATION_ASSIGN)) return staticAssign(target, name);
        if (match(GROW, SHRINK)) return staticSpecialAssign(target, name);
        return new Expr.StaticGet(target, name);
    }

    private Expr staticCall(LoxClass target, Token name) {
        consumeBracketOpen("static call");
        List<Expr> args = args();


        int ordinal = target.getStaticMethodOrdinal(name.lexeme(), argTypes(args));
        if (ordinal == -1) {
            error(name, "unknown symbol");
            consumeBracketClose("static call");
            return new Expr.StaticCall(target, name, ordinal, args);
        }

        ScriptedCallable callable = target.getStaticMethodByOrdinal(name.lexeme(), ordinal);
        checkArguments(args, callable, name);

        consumeBracketClose("static call");

        return new Expr.StaticCall(target, name, ordinal, args);
    }

    private Expr staticAssign(LoxClass target, Token name) {
        Token type = previous();
        Expr value = expression();
        Pair<LoxClass, Operand> executor = getExecutor(target.getStaticFieldType(name.lexeme()), type, value);
        return new Expr.StaticSet(target, name, value, type, executor.left(), executor.right());
    }

    private Expr staticSpecialAssign(LoxClass target, Token name) {
        return new Expr.StaticSpecial(target, name, previous());
    }

    public List<Expr> args() {
        List<Expr> arguments = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (arguments.size() > 255) error(peek(), "Can't have more than 255 arguments");
                arguments.add(expression());
            } while (match(COMMA));
        }

        return arguments;
    }

    public List<? extends LoxClass> argTypes(List<Expr> args) {
        return args.stream().map(this.finder::findRetType).toList();
    }


    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(S_BRACKET_O)) {
                Token bracketO = previous();
                Expr index = expression();
                consume(S_BRACKET_C, "']' expected");
                if (!finder.findRetType(expr).isArray()) error(bracketO, "array type expected");
                expr = new Expr.ArrayGet(expr, index);
            } else if (match(BRACKET_O)) {
                if (expr instanceof Expr.Get get) expr = finishInstCall(get);
                else error(locFinder.find(expr), "obj expected");
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'");
                LoxClass targetType = finder.findRetType(expr);
                if (!check(BRACKET_O)) { //ensure not to check for field if it's a method
                    if (!targetType.hasField(name.lexeme())) error(name, "unknown symbol");
                } else
                    if (!targetType.hasMethod(name.lexeme())) error(name, "unknown symbol");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    public void checkArguments(List<Expr> args, ScriptedCallable target, Token loc) {
        List<? extends LoxClass> expectedTypes = target.argTypes();
        List<? extends LoxClass> givenTypes = argTypes(args);
        if (expectedTypes.size() != givenTypes.size()) {
            error(loc, String.format("constructor for %s cannot be applied to given types;", loc.lexeme()));

            errorLogger.logError("required: " + expectedTypes.stream().map(LoxClass::name).collect(Collectors.joining(",")));
            errorLogger.logError("found:    " + givenTypes.stream().map(LoxClass::name).collect(Collectors.joining(",")));
            errorLogger.logError("reason: actual and formal argument lists differ in length");
        } else {
            for (int i = 0; i < givenTypes.size(); i++) {
                expectType(locFinder.find(args.get(i)), givenTypes.get(i), expectedTypes.get(i));
            }
        }

    }

    private Object literal() {
        if (match(FALSE)) return false;
        if (match(TRUE)) return true;
        if (match(NULL)) return null;

        if (match(NUM, STR)) {
            return previous().literal();
        }

        throw error(peek(), "Expected literal");
    }

    private Expr primary() {
        if (match(NEW)) {
            LoxClass loxClass = consumeVarType();
            Token loc = previous();
            consumeBracketOpen("constructors");
            List<Expr> args = args();
            consumeBracketClose("constructors");
            int ordinal = loxClass.getConstructor().getMethodOrdinal(argTypes(args));
            ScriptedCallable callable = loxClass.getConstructor().getMethodByOrdinal(ordinal);

            checkArguments(args, callable, loc);

            return new Expr.Constructor(loc, loxClass, args, ordinal);
        }

        if (match(PRIMITIVE)) {
            return new Expr.Literal(previous());
        }

        if (match(IDENTIFIER)) {
            Token previous = previous();
            if (!varAnalyser.has(previous.lexeme())) {
                current--;
                return statics();
            }
            checkVarExistence(previous, true, true);
            return new Expr.VarRef(previous);
        }

        if (match(THIS)) return new Expr.VarRef(previous());

        if (match(BRACKET_O)) {
            Expr expr = expression();
            consumeBracketClose("expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expression expected.");
    }
}
