package net.kapitencraft.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Util {

    public static <K, V> Map<K, V> mergeMaps(Map<? extends K, ? extends V> base, Map<? extends K, ? extends V> extension) {
        Map<K, V> temp = new HashMap<>(base);
        temp.putAll(extension);
        return Map.copyOf(temp);
    }

    public static void delete(File file) {
        File[] files = file.listFiles();
        if (files != null) for (File subFile : files) {
            delete(subFile);
        }
        file.delete();
    }

    //that's because Objects.requireNonNullElse checks else for null
    public static <K> K nonNullElse(K main, K other) {
        return main != null ? main : other;
    }

    public static boolean matchArgs(List<ClassReference> got, List<ClassReference> expected) {
        if (got.size() != expected.size()) {
            return false;
        }
        if (got.isEmpty()) return true;
        for (int i = 0; i < got.size(); i++) {
            if (!got.get(i).get().isChildOf(expected.get(i).get())) return false;
        }
        return true;
    }

    public static String getDescriptor(List<ClassReference> args) {
        return args.stream().map(ClassReference::name).collect(Collectors.joining(","));
    }

    /**
     * get all elements of a Map containing a Map
     */
    static <T, K, L, J extends Map<K, L>> List<L> values(Map<T, J> map) {
        return map.values().stream().map(Map::values).flatMap(Collection::stream).toList();
    }

    public static List<File> listResources(File file, @Nullable String fileSuffix) {
        if (!file.exists()) return List.of();
        if (!file.isDirectory()) return List.of(file);
        List<File> finals = new ArrayList<>();
        List<File> queue = new ArrayList<>();
        queue.add(file);
        while (!queue.isEmpty()) {
            File el = queue.get(0);
            if (el.isDirectory()) {
                File[] files = el.listFiles();
                if (files != null) queue.addAll(List.of(files));
            } else {
                if (fileSuffix == null || el.getPath().endsWith(fileSuffix)) finals.add(el);
            }
            queue.remove(0);
        }
        return finals;
    }

    /**
     * @param map the map to write
     * @param keyMapper a function to convert each key of the map into an JsonElement
     * @param valueMapper a function to convert each value of the map into an JsonElement
     * @return an JsonArray containing each entry of the map
     */
    public static <K, V> JsonArray writeMap(Map<K, V> map, Function<K, JsonElement> keyMapper, Function<V, JsonElement> valueMapper) {
        JsonArray array = new JsonArray(map.size());
        map.forEach((k, v) -> {
            JsonObject object = new JsonObject();
            object.add("key", keyMapper.apply(k));
            object.add("value", valueMapper.apply(v));
        });
        return array;
    }

    /**
     * @param array the json array containing all map entries
     * @param keyExtractor a function that extracts each map key out of the entry
     * @param valueExtractor a function that extracts each map value out of the entry
     * @return a MapStream containing all the entries extracted
     */
    public static <K, V> Map<K, V> readMap(JsonArray array, BiFunction<JsonObject, String, K> keyExtractor, BiFunction<JsonObject, String, V> valueExtractor) {
        HashMap<K, V> map = new HashMap<>();
        array.asList().stream().map(JsonElement::getAsJsonObject)
                .forEach(object -> map.put(keyExtractor.apply(object, "key"), valueExtractor.apply(object, "value")));
        return map;
    }

    public static <T> List<T> invert(List<T> parents) {
        List<T> inverted = new ArrayList<>();
        for (int i = parents.size()-1; i >= 0; i--) {
            inverted.add(parents.get(i));
        }
        return inverted;
    }

    public static <K, V, L> Collector<L, ?, List<Pair<K, V>>> toPairList(Function<L, K> keyMapper, Function<L, V> valueMapper) {
        return Collector.of(
                ArrayList::new,
                (pairs, l) -> pairs.add(new Pair<>(keyMapper.apply(l), valueMapper.apply(l))),
                (pairs, pairs2) -> {
                    pairs.addAll(pairs2);
                    return pairs;
                }
        );
    }
}
