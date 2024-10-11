package net.kapitencraft.lang.compile.parser;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.compile.Compiler;
import net.kapitencraft.lang.func.LoxCallable;
import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.token.TokenTypeCategory;
import net.kapitencraft.lang.oop.clazz.LoxClass;

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
        if (match(WHEN_CONDITION)) {
            expectCondition(expr);
            Expr ifTrue = expression();
            consume(WHEN_FALSE, "':' expected");
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
                if (assign.type() == TokenType.ASSIGN) varAnalyser.setHasValue(name.lexeme());

                return new Expr.Assign(name, value, assign);
            } else if (expr instanceof Expr.Get get) {
                LoxClass target = finder.findRetType(get.object);
                expectType(get.name, target.getFieldType(get.name.lexeme()), finder.findRetType(value));
                return new Expr.Set(get.object, get.name, value, assign);
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

    private void checkBinary(Expr leftArg, Token operator, Expr rightArg) {
        LoxClass left = finder.findRetType(leftArg);
        LoxClass right = finder.findRetType(rightArg);
        if (operator.type() == ADD && (left == VarTypeManager.STRING || right == VarTypeManager.STRING)) return;
        if (operator.type().isCategory(BOOL_BINARY) && !(left == VarTypeManager.BOOLEAN && right == VarTypeManager.BOOLEAN))
            error(operator, "both values must be boolean");
        if (operator.type().isCategory(ARITHMETIC_BINARY) && !(left.superclass() == VarTypeManager.NUMBER && right.superclass() == VarTypeManager.NUMBER))
            error(operator, "both values must be numbers");
        if (left != right)
            error(operator, "can not combine values of different types");

    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(EQUALITY)) {
            Token operator = previous();
            Expr right = comparison();
            checkBinary(expr, operator, right);
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(COMPARATORS)) {
            Token operator = previous();
            Expr right = term();
            checkBinary(expr, operator, right);
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(SUB, ADD)) {
            Token operator = previous();
            Expr right = factor();

            checkBinary(expr, operator, right);
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(DIV, MUL, MOD, POW)) {
            Token operator = previous();
            Expr right = unary();
            checkBinary(expr, operator, right);
            expr = new Expr.Binary(expr, operator, right);
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

        Expr provider = expression();

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
            }
        }

        consumeCurlyClose("switch body");
        return new Expr.Switch(provider, params, def, keyword);
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = args();

        Token bracket = consumeBracketClose("arguments");

        return new Expr.Call(callee, bracket, arguments);
    }

    private Expr finishInstCall(Expr.Get get) {
        List<Expr> arguments = args();

        List<LoxClass> givenTypes = arguments.stream().map(this.finder::findRetType).toList();
        LoxClass targetClass = this.finder.findRetType(get.object);

        int ordinal = targetClass.getMethodOrdinal(get.name.lexeme(), givenTypes);
        if (ordinal == -1) error(get.name, "unknown symbol");
        LoxCallable callable = ordinal == -1 ? null : targetClass.getMethodByOrdinal(get.name.lexeme(), ordinal);
        List<? extends LoxClass> expectedTypes = callable != null ? callable.argTypes() : List.of();
        if (expectedTypes.size() != givenTypes.size()) {
            error(get.name, String.format("method %s cannot be applied to given types;", get.name.lexeme()));

            errorLogger.logError("required: " + expectedTypes.stream().map(LoxClass::name).collect(Collectors.joining(",")));
            errorLogger.logError("found:    " + givenTypes.stream().map(LoxClass::name).collect(Collectors.joining(",")));
            errorLogger.logError("reason: actual and formal argument lists differ in length");
        } else {
            for (int i = 0; i < givenTypes.size(); i++) {
                expectType(locFinder.find(arguments.get(i)), givenTypes.get(i), expectedTypes.get(i));
            }
        }
        Token bracket = consumeBracketClose("arguments");

        return new Expr.InstCall(get.object, get.name, ordinal, bracket, arguments);
    }

    private List<Expr> args() {
        List<Expr> arguments = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (arguments.size() > 255) error(peek(), "Can't have more than 255 arguments");
                arguments.add(expression());
            } while (match(COMMA));
        }

        return arguments;
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(BRACKET_O)) {
                if (expr instanceof Expr.Get get) expr = finishInstCall(get);
                    else expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                if (!check(BRACKET_O)) { //ensure not to check for field if it's a method
                    if (!finder.findRetType(expr).hasField(name.lexeme())) error(name, "unknown symbol");
                }
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
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
            consumeBracketOpen("constructor");
            List<Expr> args = args();
            consumeBracketClose("constructor");
            int ordinal = loxClass.getConstructor().getMethodOrdinal(args.stream().map(this.finder::findRetType).toList());
            LoxCallable callable = loxClass.getConstructor().getMethodByOrdinal(ordinal);
            List<? extends LoxClass> expectedTypes = callable.argTypes();
            List<LoxClass> givenTypes = args.stream().map(this.finder::findRetType).toList();
            if (expectedTypes.size() != givenTypes.size()) {
                error(loc, String.format("method %s cannot be applied to given types;", loc.lexeme()));

                errorLogger.logError("required: " + expectedTypes.stream().map(LoxClass::name).collect(Collectors.joining(",")));
                errorLogger.logError("found:    " + givenTypes.stream().map(LoxClass::name).collect(Collectors.joining(",")));
                errorLogger.logError("reason: actual and formal argument lists differ in length");
            } else {
                for (int i = 0; i < givenTypes.size(); i++) {
                    expectType(locFinder.find(args.get(i)), givenTypes.get(i), expectedTypes.get(i));
                }
            }

            return new Expr.Constructor(loxClass, args, ordinal);
        }

        if (match(PRIMITIVE)) {
            return new Expr.Literal(previous());
        }

        if (match(IDENTIFIER)) {
            Token previous = previous();
            if (peek().type() == BRACKET_O) {
                return new Expr.FuncRef(previous);
            } else {
                checkVarExistence(previous, true, true);
                return new Expr.VarRef(previous);
            }
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
