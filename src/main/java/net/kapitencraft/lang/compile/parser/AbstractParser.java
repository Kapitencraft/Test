package net.kapitencraft.lang.compile.parser;

import net.kapitencraft.lang.compile.Compiler;
import net.kapitencraft.lang.compile.VarTypeParser;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.oop.clazz.LoxClass;

import static net.kapitencraft.lang.holder.token.TokenType.*;
import static net.kapitencraft.lang.holder.token.TokenType.EOA;

@SuppressWarnings("ThrowableNotThrown")
public class AbstractParser {
    protected int current;
    protected Token[] tokens;
    protected VarTypeParser parser;
    protected final Compiler.ErrorLogger errorLogger;

    public AbstractParser(Compiler.ErrorLogger errorLogger) {
        this.errorLogger = errorLogger;
    }

    public void apply(Token[] toParse, VarTypeParser targetAnalyser) {
        this.current = 0;
        this.tokens = toParse;
        this.parser = targetAnalyser;
    }

    protected boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    protected boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    protected boolean isAtEnd() {
        return current == tokens.length - 1;
    }

    protected Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    protected Token peek() {
        return tokens[current];
    }

    protected Token previous() {
        return tokens[current - 1];
    }


    protected Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    protected LoxClass consumeVarType() {
        Token token = consume(IDENTIFIER, "<identifier> expected");
        LoxClass loxClass = parser.getClass(token.lexeme);
        if (loxClass == null) error(token, "unknown symbol");
        return loxClass;
    }

    protected Token consumeIdentifier() {
        return consume(IDENTIFIER, "<identifier> expected");
    }

    protected Token consumeBracketOpen(String method) {
        return this.consume(BRACKET_O, "Expected '(' after '" + method + "'.");
    }

    protected Token consumeCurlyOpen(String method) {
        return this.consume(C_BRACKET_O, "Expected '{' after '" + method + "'.");
    }

    protected Token consumeCurlyClose(String method) {
        return this.consume(C_BRACKET_C, "Expected '}' after " + method + ".");
    }

    protected Token consumeBracketClose(String method) {
        return this.consume(BRACKET_C, "Expected ')' after " + method + ".");
    }

    protected Token consumeEndOfArg() {
        return this.consume(EOA, "';' expected");
    }

    protected static class ParseError extends RuntimeException {}

    protected ParseError error(Token token, String message) {
        errorLogger.error(token, message);
        return new ParseError();
    }

    protected void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == EOA) return;

            switch (peek().type) {
                case CLASS:
                case FUNC:
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
