package net.kapitencraft.lang.compile;

import net.kapitencraft.lang.ast.Expr;
import net.kapitencraft.lang.run.Main;
import net.kapitencraft.lang.ast.Token;
import net.kapitencraft.lang.ast.TokenType;
import net.kapitencraft.lang.ast.Stmt;

import java.util.ArrayList;
import java.util.List;
import static net.kapitencraft.lang.ast.TokenType.*;

@SuppressWarnings({"UnusedReturnValue", "ThrowableNotThrown"})
public class Parser {
    private final List<Token> tokens;
    private final String[] lines;
    private int current = 0;

    Parser(List<Token> tokens, String[] lines) {
        this.tokens = tokens;
        this.lines = lines;
    }

    List<Stmt> parse() {
        System.out.println("Parsing...");
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(VAR)) return varDeclaration();
            if (match(FUNC)) return funcDecl("function");

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expected variable name.");

        Expr initializer = null;
        if (match(ASSIGN)) {
            initializer = expression();
        }

        consume(EOA, "Expected ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt whileStatement() {
        consumeBracketOpen("while");
        Expr condition = expression();
        consumeBracketClose("while condition");
        Stmt body = statement();

        return new Stmt.While(condition, body);
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
        consume(EOA, "Expected ';' after " + token.lexeme + " statement");
        return new Stmt.LoopInterruption(token);
    }

    private Stmt forStatement() {
        consumeBracketOpen("for");

        Stmt initializer;
        if (match(EOA)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(EOA)) {
            condition = expression();
        }
        consume(EOA, "Expected ';' after loop condition.");

        Expr increment = null;
        if (!check(BRACKET_C)) {
            increment = expression();
        }
        consumeBracketClose("for clauses");

        Stmt body = statement();

        return new Stmt.For(initializer, condition, increment, body);
    }

    private Stmt ifStatement() {
        consumeBracketOpen("if");
        Expr condition = expression();
        consumeBracketClose("if condition");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(EOA)) {
            value = expression();
        }

        consume(EOA, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(EOA, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt.Function funcDecl(String kind) {
        Token type = consume(VAR_TYPE, "Expected Var type.");
        Token name = consume(IDENTIFIER, "Expected " + kind + " name.");

        consumeBracketOpen(kind + " name");
        List<Token> parameters = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }

                parameters.add(consume(IDENTIFIER, "Expected parameter name."));
            } while (match(COMMA));
        }
        consumeBracketClose("parameters");

        consume(C_BRACKET_O, "Expected '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(type, name, parameters, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(C_BRACKET_C) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(C_BRACKET_C, "Expected '}' after block.");
        return statements;
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

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(ASSIGN, ADD_ASSIGN, SUB_ASSIGN, MUL_ASSIGN, DIV_ASSIGN, MOD)) {
            Token assign = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable variable) {
                Token name = variable.name;

                return new Expr.Assign(name, value, assign);
            }

            error(assign, "Invalid assignment target.");
        }

        if (match(GROW, SHRINK)) {
            Token assign = previous();

            if (expr instanceof Expr.Variable variable) {
                Token name = variable.name;

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

        return call();
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(BRACKET_C)) {
            do {
                if (arguments.size() > 255) error(peek(), "Can't have more than 255 arguments");
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consumeBracketClose("arguments");

        return new Expr.Call(callee, paren, arguments);
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

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NULL)) return new Expr.Literal(null);

        if (match(NUM, STR)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            Token previous = previous();
            if (peek().type == BRACKET_O) {
                return new Expr.Function(previous);
            } else {
                return new Expr.Variable(previous);
            }
        }

        if (match(BRACKET_O)) {
            Expr expr = expression();
            consumeBracketClose("expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expression expected.");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private Token consumeBracketOpen(String method) {
        return this.consume(BRACKET_O, "Expected '(' after '" + method + "'.");
    }

    private Token consumeBracketClose(String method) {
        return this.consume(BRACKET_C, "Expected ')' after " + method + ".");
    }

    private static class ParseError extends RuntimeException {}

    private ParseError error(Token token, String message) {
        Main.error(token, message, lines[token.line - 1]);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == EOA) return;

            switch (peek().type) {
                case CLASS:
                case FUNC:
                case VAR:
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
