package cn.crane4j.core.util;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * A map that can hold multiple values for a key.
 *
 * @author huangchengxing
 * @see StandardMultiMap
 */
public interface MultiMap<K, V> {

    /**
     * Create a new {@link MultiMap} instance with {@link HashMap}
     * as the underlying map and {@link ArrayList} as the collection.
     *
     * @return a new {@link MultiMap} instance
     * @see HashMap
     * @see ArrayList
     */
    static <K, V> MultiMap<K, V> arrayListMultimap() {
        return new StandardMultiMap<>(new HashMap<>(8), ArrayList::new);
    }

    /**
     * Create a new {@link MultiMap} instance with {@link LinkedHashMap}
     * as the underlying map and {@link ArrayList} as the collection.
     *
     * @return a new {@link MultiMap} instance
     * @see LinkedHashMap
     * @see ArrayList
     */
    static <K, V> MultiMap<K, V> linkedListMultimap() {
        return new StandardMultiMap<>(new LinkedHashMap<>(), ArrayList::new);
    }

    /**
     * Create a new {@link MultiMap} instance with {@link LinkedHashMap}
     * as the underlying map and {@link LinkedHashSet} as the collection.
     *
     * @return a new {@link MultiMap} instance
     * @see LinkedHashMap
     * @see LinkedHashSet
     */
    static <K, V> MultiMap<K, V> linkedHashMultimap() {
        return new StandardMultiMap<>(new LinkedHashMap<>(), LinkedHashSet::new);
    }

    /**
     * Get the total number of key-value pairs in the map.
     *
     * @return the total number of key-value pairs in the map
     */
    int size();

    /**
     * Whether the map is empty.
     *
     * @return whether the map is empty
     */
    boolean isEmpty();

    /**
     * Whether the map contains the specified key.
     *
     * @param o key
     * @return whether the map contains the specified key
     */
    boolean containsKey(Object o);

    /**
     * Put the specified key-value pair into the map.
     *
     * @param k key
     * @param v value
     * @return whether the map has changed
     */
    boolean put(K k, V v);

    /**
     * Put all key-value pairs in the specified map into the map.
     *
     * @param k key
     * @param iterable values
     */
    void putAll(K k, Iterable<? extends V> iterable);

    /**
     * Put all key-value pairs in the specified map into the map.
     *
     * @param multiMap map
     */
    void putAll(MultiMap<K, V> multiMap);

    /**
     * Remove all key-value pairs with the specified key from the map.
     *
     * @param o key
     * @return all values of the specified key
     */
    Collection<V> removeAll(Object o);

    /**
     * Clear the map and remove all key-value pairs.
     */
    void clear();

    /**
     * Get all values of the specified key, if the key does not exist, return an empty collection.
     *
     * @param k key
     * @return all values of the specified key
     */
    Collection<V> get(K k);

    /**
     * Get all keys in the map.
     *
     * @return all keys in the map
     */
    Set<K> keySet();

    /**
     * Get all values in the map.
     *
     * @return all values in the map
     */
    Collection<V> values();

    /**
     * <p>Get all key-value pairs in the map.<br />
     * The returned entries are modifiable, but the modification will not affect the map.
     *
     * @return all key-value pairs in the map
     */
    Collection<Map.Entry<K,V>> entries();

    /**
     * Traverse all key-value pairs in the map.
     *
     * @param action action
     */
    @SuppressWarnings("unchecked")
    default void forEach(BiConsumer<? super K,? super V> action) {
        asMap().forEach((k, vs) -> vs.forEach(v -> action.accept(k, v)));
    }

    /**
     * Get java map of the multimap, the returned map is modifiable.
     *
     * @return java map of the multi map
     */
    Map<K, Collection<V>> asMap();

    /**
     * <p>Whether the map is equal to the specified object.<br />
     * The result is true if and only if the specified object is also a {@link MultiMap},
     * and the two maps have the same result for equals for java map returned by {@link #asMap()}.
     *
     * @param o object
     * @return whether the map is equal to the specified object
     */
    boolean equals(Object o);

    /**
     * <p>Get the hash code of the map.<br />
     * The hash code is defined as the hash code of the java map returned by {@link #asMap()}.
     *
     * @return the hash code of the map
     */
    int hashCode();
}
