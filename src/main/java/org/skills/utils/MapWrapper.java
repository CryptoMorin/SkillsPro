package org.skills.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MapWrapper<K, V> implements Map<K, V> {
    private final Map<K, V> map;

    public MapWrapper(Map<K, V> map) {
        this.map = Objects.requireNonNull(map);
    }

    @Override
    public int size() {
        StringUtils.printStackTrace();
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        StringUtils.printStackTrace();
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        StringUtils.printStackTrace();
        return map.containsKey(key);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean containsValue(Object value) {
        StringUtils.printStackTrace();
        return map.containsKey(value);
    }

    @Override
    public V get(Object key) {
        StringUtils.printStackTrace();
        return map.get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        StringUtils.printStackTrace();
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        StringUtils.printStackTrace();
        return map.remove(key);
    }

    @Override
    public void putAll(@Nonnull Map<? extends K, ? extends V> m) {
        StringUtils.printStackTrace();
        map.putAll(m);
    }

    @Override
    public void clear() {
        StringUtils.printStackTrace();
        map.clear();
    }

    @Nonnull
    @Override
    public Set<K> keySet() {
        StringUtils.printStackTrace();
        return map.keySet();
    }

    @Nonnull
    @Override
    public Collection<V> values() {
        StringUtils.printStackTrace();
        return map.values();
    }

    @Nonnull
    @Override
    public Set<Entry<K, V>> entrySet() {
        StringUtils.printStackTrace();
        return map.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        StringUtils.printStackTrace();
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        StringUtils.printStackTrace();
        map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        StringUtils.printStackTrace();
        map.replaceAll(function);
    }

    @Nullable
    @Override
    public V putIfAbsent(K key, V value) {
        StringUtils.printStackTrace();
        return map.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        StringUtils.printStackTrace();
        return map.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        StringUtils.printStackTrace();
        return map.replace(key, oldValue, newValue);
    }

    @Nullable
    @Override
    public V replace(K key, V value) {
        StringUtils.printStackTrace();
        return map.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, @Nonnull Function<? super K, ? extends V> mappingFunction) {
        StringUtils.printStackTrace();
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, @Nonnull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        StringUtils.printStackTrace();
        return map.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, @Nonnull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        StringUtils.printStackTrace();
        return map.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, @Nonnull V value, @Nonnull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        StringUtils.printStackTrace();
        return map.merge(key, value, remappingFunction);
    }
}
