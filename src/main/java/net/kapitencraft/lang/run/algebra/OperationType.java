package net.kapitencraft.lang.run.algebra;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kapitencraft.lang.holder.token.Token;
import net.kapitencraft.lang.holder.token.TokenType;
import net.kapitencraft.lang.holder.token.TokenTypeCategory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static OperationType of(Token operator) {
        for (OperationType type : values()) {
            if (type.type.contains(operator.type())) return type;
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
