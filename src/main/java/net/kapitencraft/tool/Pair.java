package net.kapitencraft.tool;

import com.google.gson.JsonObject;

import java.util.function.Function;

public record Pair<T, K>(T left, K right) {

    public static <T, K> Pair<T, K> of(T left, K right) {
        return new Pair<>(left, right);
    }
}
