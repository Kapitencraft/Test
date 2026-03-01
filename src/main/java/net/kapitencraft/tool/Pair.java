package net.kapitencraft.tool;

import java.util.function.Function;

public final class Pair<F, S> {
    private final F first;
    private final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <T, K> Pair<T, K> of(T left, K right) {
        return new Pair<>(left, right);
    }

    public <F1> Pair<F1, S> mapFirst(Function<F, F1> mapper) {
        return Pair.of(mapper.apply(this.first), this.second);
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "Pair[" +
                "left=" + first + ", " +
                "right=" + second + ']';
    }

}
