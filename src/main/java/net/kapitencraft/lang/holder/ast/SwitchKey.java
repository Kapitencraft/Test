package net.kapitencraft.lang.holder.ast;

import net.kapitencraft.lang.holder.token.Token;

public abstract class SwitchKey {
    private final Token source;
    public final Expr expr;
    public int index;

    protected SwitchKey(Token source, Expr expr) {
        this.source = source;
        this.expr = expr;
    }

    public static class Number extends SwitchKey {
        public Number(Token source, Expr expr) {
            super(source, expr);
        }
    }

    public static class String extends SwitchKey {
        public String(Token source, Expr expr) {
            super(source, expr);
        }
    }

    public static class Identifier extends SwitchKey {
        public Identifier(Token source, Expr expr) {
            super(source, expr);
        }
    }

    public static class Illegal extends SwitchKey {

        public Illegal(Token source, Expr expr) {
            super(source, expr);
        }
    }
}
