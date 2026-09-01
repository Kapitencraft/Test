package net.kapitencraft.tool;

import java.util.function.Function;

public record Pair<F, S>(F first, S second) {

    public static <T, K> Pair<T, K> of(T left, K right) {
        return new Pair<>(left, right);
    }

    public <F1> Pair<F1, S> mapFirst(Function<F, F1> mapper) {
        return Pair.of(mapper.apply(this.first), this.second);
    }

    @Override
    public String toString() {
        return "Pair[" +
                "left=" + first + ", " +
                "right=" + second + ']';
    }

}
