package net.kapitencraft.lang.compile;

import net.kapitencraft.lang.VarTypeManager;
import net.kapitencraft.lang.run.Main;
import net.kapitencraft.lang.ast.token.Token;
import net.kapitencraft.lang.ast.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.kapitencraft.lang.ast.token.TokenType.*;

public class LangScanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private final String[] lines;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int indexAtLineStart = 0;

    private void nextLine() {
        line++;
        indexAtLineStart = current + 1;
    }

    public LangScanner(String source) {
        this.source = source;
        this.lines = source.split("\n", Integer.MAX_VALUE);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, start - indexAtLineStart + 1));
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line, current - indexAtLineStart + 1));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(BRACKET_O); break;
            case ')': addToken(BRACKET_C); break;
            case '{': addToken(C_BRACKET_O); break;
            case '}': addToken(C_BRACKET_C); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case ';': addToken(EOA); break;
            case '-':

                addToken(match('>') ? LAMBDA : match('-') ? SHRINK : match('=') ? SUB_ASSIGN : SUB);
                break;
            case '+':
                addToken(match('+') ? GROW : match('=') ? ADD_ASSIGN : ADD);
                break;
            case '*':
                addToken(match('=') ? MUL_ASSIGN : MUL);
                break;
            case '%':
                addToken(match('=') ? MOD_ASSIGN : MOD);
            case '!':
                addToken(match('=') ? NEQUAL : NOT);
                break;
            case '=':
                addToken(match('=') ? EQUAL : ASSIGN);
                break;
            case '<':
                addToken(match('=') ? LEQUAL : LESSER);
                break;
            case '>':
                addToken(match('=') ? GEQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(match('=') ? DIV_ASSIGN : DIV);
                }
                break;
            case ' ':
            case '\t':
                //addToken(IN_LINE);
            case '\r':
                // Ignore whitespace.
                break;
            case '\n':
                nextLine();
                break;
            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error("Unexpected character");
                }
        }
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private String getCurrentLine() {
        return this.lines[line - 1];
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9' || c == '.';
    }

    private void number() {
        boolean seenDecimal = match('.');
        current--; //jump back
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (!seenDecimal && peek() == '.' && isDigit(peekNext())) {

            do advance();
            while (isDigit(peek()));
        }

        String literal = source.substring(start, current);
        if (seenDecimal) addToken(NUM, Double.parseDouble(literal));
        else addToken(NUM, Integer.parseInt(literal));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        addToken(VarTypeManager.getType(text));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') nextLine();
            advance();
        }

        if (isAtEnd()) {
            error("Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STR, value);
    }

    private void error(String msg) {
        Main.error(line, msg, getCurrentLine());
    }

    public String[] getLines() {
        return this.lines;
    }
}