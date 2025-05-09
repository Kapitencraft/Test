package net.kapitencraft.lang.run.algebra;

import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.token.TokenTypeCategory;

import java.util.List;

public enum OperationType {
    ADDITION(TokenType.ADD, TokenType.ADD_ASSIGN),
    SUBTRACTION(TokenType.SUB, TokenType.SUB_ASSIGN),
    MULTIPLICATION(TokenType.MUL, TokenType.MUL_ASSIGN),
    DIVISION(TokenType.DIV, TokenType.DIV_ASSIGN),
    MODULUS(TokenType.MOD, TokenType.MOD_ASSIGN),
    POTENCY(TokenType.POW, TokenType.POW_ASSIGN),
    LEQUAL(TokenType.LEQUAL),
    NEQUAL(TokenType.NEQUAL),
    GEQUAL(TokenType.GEQUAL),
    LESS(TokenType.LESSER), // =
    MORE(TokenType.GREATER),
    EQUAL(TokenType.EQUAL);

    private final List<TokenType> type;


    OperationType(TokenType... type) {
        this.type = List.of(type);
    }

    public List<TokenType> getType() {
        return type;
    }

    public static OperationType of(TokenType operator) {
        for (OperationType type : values()) {
            if (type.type.contains(operator)) return type;
        }
        return null;
    }

    public boolean is(TokenTypeCategory category) {
        return this.type.stream().anyMatch(t -> t.isCategory(category));
    }

    public boolean isComparator() {
        return is(TokenTypeCategory.COMPARATORS) || is(TokenTypeCategory.EQUALITY);
    }
}
