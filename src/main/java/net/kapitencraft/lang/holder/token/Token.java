package net.kapitencraft.lang.holder.token;

import net.kapitencraft.lang.holder.LiteralHolder;
import net.kapitencraft.lang.holder.ast.Expr;

@SuppressWarnings("ClassCanBeRecord") //stfu intellij
public class Token {
    public final TokenType type;
    public final LiteralHolder literal;
    public final int line;
    public final int lineStartIndex;
    public final String lexeme;

    public Token(TokenType type, String lexeme, LiteralHolder literal, int line, int lineStartIndex) {
        this.type = type;
        this.literal = literal;
        this.line = line;
        this.lexeme = lexeme;
        this.lineStartIndex = lineStartIndex;
    }

    public String toString() {
        return String.format("Token{type=%s, lexeme=%s, literal=%s}@line%s", type, lexeme, literal, line);
    }
}
