package net.kapitencraft.lang.compile;

import net.kapitencraft.lang.holder.ast.Expr;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.ast.Stmt;
import net.kapitencraft.tool.Pair;

import java.util.*;

import static net.kapitencraft.lang.holder.token.TokenType.*;

@SuppressWarnings({"UnusedReturnValue", "ThrowableNotThrown"})
public class Parser {
    private final List<Token> tokens;
    private final String[] lines;
    private int current = 0;
    private final Compiler.ErrorConsumer errorConsumer;

    Parser(List<Token> tokens, String[] lines, Compiler.ErrorConsumer errorConsumer) {
        this.tokens = tokens;
        this.lines = lines;
        this.errorConsumer = errorConsumer;
    }

    List<Stmt> parse() {
        System.out.println("Parsing...");
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements.stream().filter(Objects::nonNull).toList(); //clear null elements
    }

    private Stmt declaration() {
        try {
            if (match(VAR_TYPE)) return varDeclaration();
            if (match(CLASS)) return classDeclaration();
            if (match(FUNC)) return funcDecl("function");

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        consume(C_BRACKET_O, "Expect '{' before class body.");

        List<Stmt.FuncDecl> methods = new ArrayList<>();
        while (!check(C_BRACKET_C) && !isAtEnd()) {
            methods.add(funcDecl("method"));
        }

        consumeCurlyClose("class body");

        return new Stmt.Class(name, methods);
    }

    private Stmt varDeclaration() {
        Token type = previous();
        Token name = consume(IDENTIFIER, "Expected variable name.");

        Expr initializer = null;
        if (match(ASSIGN)) {
            initializer = expression();
        }

        consumeEndOfArg();
        return new Stmt.VarDecl(name, type, initializer);
    }

    private Stmt whileStatement() {
        Token keyword = previous();
        consumeBracketOpen("while");
        Expr condition = expression();
        consumeBracketClose("while condition");
        Stmt body = statement();

        return new Stmt.While(condition, body, keyword);
    }

    private Stmt statement() {
        if (match(RETURN)) return returnStatement();
        if (match(CONTINUE, BREAK)) return loopInterruptionStatement();
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(WHILE)) return whileStatement();
        if (match(C_BRACKET_O)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt loopInterruptionStatement() {
        Token token = previous();
        consumeEndOfArg();
        return new Stmt.LoopInterruption(token);
    }

    private Stmt forStatement() {
        Token keyword = previous();

        consumeBracketOpen("for");

        Stmt initializer;
        if (match(EOA)) {
            initializer = null;
        } else if (match(VAR_TYPE)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(EOA)) {
            condition = expression();
        }
        consumeEndOfArg();

        Expr increment = null;
        if (!check(BRACKET_C)) {
            increment = expression();
        }
        consumeBracketClose("for clauses");

        Stmt body = statement();

        return new Stmt.For(initializer, condition, increment, body, keyword);
    }

    private Stmt ifStatement() {
        Token statement = previous();
        consumeBracketOpen("if");
        Expr condition = expression();
        consumeBracketClose("if condition");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        List<Pair<Expr, Stmt>> elifs = new ArrayList<>();
        while (match(ELIF)) {
            consumeBracketOpen("elif");
            Expr elifCondition = expression();
            consumeBracketClose("elif condition");
            Stmt elifStmt = statement();
            elifs.add(Pair.of(elifCondition, elifStmt));
        }

        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch, elifs, statement);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(EOA)) {
            value = expression();
        }

        consumeEndOfArg();
        return new Stmt.Return(keyword, value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consumeEndOfArg();
        return new Stmt.Expression(expr);
    }

    private Stmt.FuncDecl funcDecl(String kind) {
        Token type = consume(VAR_TYPE, "Expected Var type.");
        Token name = consume(IDENTIFIER, "Expected " + kind + " name.");

        consumeBracketOpen(kind + " name");
        List<Pair<Token, Token>> parameters = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }

                Token pType = consume(VAR_TYPE, "Expected Var type before parameter name");
                Token pName = consume(IDENTIFIER, "Expected parameter name.");
                parameters.add(Pair.of(pType, pName));
            } while (match(COMMA));
        }
        consumeBracketClose("parameters");

        consumeCurlyOpen(kind + " body");
        Stmt body = statement();
        consumeCurlyClose(kind + " body");
        return new Stmt.FuncDecl(type, name, parameters, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            statements.add(declaration());
        }

        consumeCurlyClose("block");
        return statements;
    }

    private Expr expression() {
        return when();
    }

    private Expr when() {
        Expr expr = assignment();
        if (match(WHEN_CONDITION)) {
            Expr ifTrue = expression();
            consume(WHEN_FALSE, "':' expected");
            Expr ifFalse = expression();
            expr = new Expr.When(expr, ifTrue, ifFalse);
        }

        return expr;
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(ASSIGN, ADD_ASSIGN, SUB_ASSIGN, MUL_ASSIGN, DIV_ASSIGN, MOD_ASSIGN)) {
            Token assign = previous();
            Expr value = assignment();

            if (expr instanceof Expr.VarRef variable) {
                Token name = variable.name;

                return new Expr.Assign(name, value, assign);
            }

            error(assign, "Invalid assignment target.");
        }

        if (match(GROW, SHRINK)) {
            Token assign = previous();

            if (expr instanceof Expr.VarRef ref) {
                Token name = ref.name;

                return new Expr.SpecialAssign(name, assign);
            }

        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(NEQUAL, EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GEQUAL, LESSER, LEQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(SUB, ADD)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(DIV, MUL, MOD)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(NOT, SUB)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        if (match(SWITCH)) {
            return switchExpr();
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
                if (params.containsKey(key)) error(previous(), "Duplicate case key '" + previous().lexeme + "'");
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
        List<Expr> arguments = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (arguments.size() > 255) error(peek(), "Can't have more than 255 arguments");
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token args = consumeBracketClose("arguments");

        return new Expr.Call(callee, args, arguments);
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(BRACKET_O)) {
                expr = finishCall(expr);
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
            return previous().literal;
        }
        throw error(peek(), "Expected literal");
    }

    private Expr primary() {

        if (match(FALSE, TRUE, NULL, NUM, STR)) {
            return new Expr.Literal(previous());
        }

        if (match(IDENTIFIER)) {
            Token previous = previous();
            if (peek().type == BRACKET_O) {
                return new Expr.FuncRef(previous);
            } else {
                return new Expr.VarRef(previous);
            }
        }

        if (match(BRACKET_O)) {
            Expr expr = expression();
            consumeBracketClose("expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expression expected.");
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }


    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private Token consumeBracketOpen(String method) {
        return this.consume(BRACKET_O, "Expected '(' after '" + method + "'.");
    }

    private Token consumeCurlyOpen(String method) {
        return this.consume(C_BRACKET_O, "Expected '{' after '" + method + "'.");
    }

    private Token consumeCurlyClose(String method) {
        return this.consume(C_BRACKET_C, "Expected '}' after " + method + ".");
    }

    private Token consumeBracketClose(String method) {
        return this.consume(BRACKET_C, "Expected ')' after " + method + ".");
    }

    private Token consumeEndOfArg() {
        return this.consume(EOA, "';' expected");
    }

    private static class ParseError extends RuntimeException {}

    private ParseError error(Token token, String message) {
        errorConsumer.error(token, message, lines[token.line - 1]);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == EOA) return;

            switch (peek().type) {
                case CLASS:
                case FUNC:
                case VAR_TYPE:
                case FOR:
                case IF:
                case WHILE:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
