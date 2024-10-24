package net.kapitencraft.tool;

public record Pair<T, K>(T left, K right) {

    public static <T, K> Pair<T, K> of(T left, K right) {
        return new Pair<>(left, right);
    }
}
