package net.kapitencraft.lang.exception;

import net.kapitencraft.lang.ast.Token;

public class EscapeLoop extends RuntimeException {
    public final Token token;

    public EscapeLoop(Token token) {
        super(null, null, false, false);

        this.token = token;
    }
}
