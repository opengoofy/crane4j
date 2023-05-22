package cn.crane4j.core.util;

import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A standard implementation of {@link MultiMap}
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class StandardMultiMap<K, V, C extends Collection<V>> implements MultiMap<K, V> {

    /**
     * Raw map.
     */
    private final Map<K, C> rawMap;

    /**
     * Collection factory.
     */
    private final Supplier<C> collectionFactory;

    /**
     * Get the total number of key-value pairs in the map.
     *
     * @return the total number of key-value pairs in the map
     */
    @Override
    public int size() {
        return rawMap.size();
    }

    /**
     * Whether the map is empty.
     *
     * @return whether the map is empty
     */
    @Override
    public boolean isEmpty() {
        return rawMap.isEmpty();
    }

    /**
     * Whether the map contains the specified key.
     *
     * @param o key
     * @return whether the map contains the specified key
     */
    @Override
    public boolean containsKey(Object o) {
        return rawMap.containsKey(o);
    }

    /**
     * Put the specified key-value pair into the map.
     *
     * @param k key
     * @param v value
     * @return whether the map has changed
     */
    @Override
    public boolean put(K k, V v) {
        return rawMap.computeIfAbsent(k, key -> collectionFactory.get()).add(v);
    }

    /**
     * Put all key-value pairs in the specified map into the map.
     *
     * @param k        key
     * @param iterable values
     */
    @Override
    public void putAll(K k, Iterable<? extends V> iterable) {
        iterable.forEach(v -> put(k, v));
    }

    /**
     * Put all key-value pairs in the specified map into the map.
     *
     * @param multiMap map
     */
    @Override
    public void putAll(MultiMap<K, V> multiMap) {
        multiMap.asMap().forEach(this::putAll);
    }

    /**
     * Remove all key-value pairs with the specified key from the map.
     *
     * @param o key
     * @return all values of the specified key
     */
    @Override
    public Collection<V> removeAll(Object o) {
        C values = rawMap.remove(o);
        return values == null ? Collections.emptyList() : values;
    }

    /**
     * Clear the map and remove all key-value pairs.
     */
    @Override
    public void clear() {
        rawMap.clear();
    }

    /**
     * Get all values of the specified key, if the key does not exist, return an empty collection.
     *
     * @param k key
     * @return all values of the specified key
     */
    @Override
    public Collection<V> get(K k) {
        return rawMap.getOrDefault(k, collectionFactory.get());
    }

    /**
     * Get all keys in the map.
     *
     * @return all keys in the map
     */
    @Override
    public Set<K> keySet() {
        return rawMap.keySet();
    }

    /**
     * Get all values in the map.
     *
     * @return all values in the map
     */
    @Override
    public Collection<V> values() {
        return rawMap.values().stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * <p>Get all key-value pairs in the map.<br />
     * The returned entries are modifiable, but the modification will not affect the map.
     *
     * @return all key-value pairs in the map
     */
    @Override
    public Collection<Map.Entry<K, V>> entries() {
        return rawMap.entrySet().stream()
            .flatMap(e -> e.getValue().stream().map(v -> new AbstractMap.SimpleEntry<>(e.getKey(), v)))
            .collect(Collectors.toList());
    }

    /**
     * Get java map of the multimap, the returned map is modifiable.
     *
     * @return java map of the multi map
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<K, Collection<V>> asMap() {
        return (Map<K, Collection<V>>)rawMap;
    }
}
