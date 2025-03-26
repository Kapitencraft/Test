package net.kapitencraft.lang.exception;

import net.kapitencraft.lang.holder.token.TokenType;

public class EscapeLoop extends RuntimeException {
    public final TokenType type;

    public EscapeLoop(TokenType type) {
        super(null, null, false, false);

        this.type = type;
    }
}
