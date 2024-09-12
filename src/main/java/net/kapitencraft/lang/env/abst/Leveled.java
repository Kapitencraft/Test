package net.kapitencraft.lang.env.abst;

import java.util.HashMap;
import java.util.Map;

public class Leveled<K, V> extends DequeStack<Map<K, V>> {

    protected Leveled() {
        super(HashMap::new);
    }

    protected V getValue(K name) {
        return getLast().get(name);
    }

    protected void addValue(K key, V value) {
        getLast().put(key, value);
    }

    protected int has(K key) {
        for (int i = 0; i < this.size()) {
            if (get(i))
        }
        return this.getLast().containsKey(key);
    }
}