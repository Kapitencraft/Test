package net.kapitencraft.lang.ast;

@SuppressWarnings("ClassCanBeRecord") //stfu intellij
public class Token {
    public final TokenType type;
    public final Object literal;
    public final int line;
    public final int lineStartIndex;
    public final String lexeme;

    public Token(TokenType type, String lexeme, Object literal, int line, int lineStartIndex) {
        this.type = type;
        this.literal = literal;
        this.line = line;
        this.lexeme = lexeme;
        this.lineStartIndex = lineStartIndex;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
