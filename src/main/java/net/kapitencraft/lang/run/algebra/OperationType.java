package net.kapitencraft.lang.run.algebra;

import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.token.TokenTypeCategory;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum OperationType {
    ADDITION(TokenType.ADD),
    SUBTRACTION(TokenType.SUB),
    MULTIPLICATION(TokenType.MUL),
    DIVISION(TokenType.DIV),
    MODULUS(TokenType.MOD),
    POTENCY(TokenType.POW),
    LEQUAL(TokenType.LEQUAL),
    NEQUAL(TokenType.NEQUAL),
    GEQUAL(TokenType.GEQUAL),
    LESS(TokenType.LESSER), // =
    MORE(TokenType.GREATER),
    EQUAL(TokenType.EQUAL);

    private final TokenType type;


    OperationType(TokenType type) {
        this.type = type;
    }

    public TokenType getType() {
        return type;
    }

    private static final Map<TokenType, OperationType> operationForToken = Arrays.stream(OperationType.values()).collect(Collectors.toMap(OperationType::getType, Function.identity()));

    public static OperationType of(Token operator) {
        return operationForToken.get(operator.type());
    }

    public boolean is(TokenTypeCategory category) {
        return this.type.isCategory(category);
    }

    public boolean isComparator() {
        return is(TokenTypeCategory.COMPARATORS) || is(TokenTypeCategory.EQUALITY);
    }
}
