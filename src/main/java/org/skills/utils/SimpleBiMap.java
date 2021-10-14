package org.skills.utils;

import java.util.HashMap;
import java.util.Map;

public class SimpleBiMap<K, V> extends HashMap<K, V> {
    private final Map<V, K> inverse = new HashMap<>();

    @Override
    public V put(K key, V value) {
        inverse.put(value, key);
        return super.put(key, value);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean containsValue(Object value) {
        return inverse.containsKey(value);
    }

    public K getInverse(V value) {
        return inverse.get(value);
    }

    @Override
    public V remove(Object key) {
        V value = super.remove(key);
        inverse.remove(value);
        return value;
    }

    public K removeInverse(V value) {
        K key = inverse.remove(value);
        super.remove(key);
        return key;
    }
}
