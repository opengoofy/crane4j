package cn.crane4j.core.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An empty {@link MultiMap} implementation,
 * which does not support any modification operations, and all values obtained are empty collections.
 *
 * @author huangchengxing
 */
public class EmptyMultiMap<K, V> implements MultiMap<K, V> {

    /**
     * instance。
     */
    @SuppressWarnings("rawtypes")
    public static final EmptyMultiMap INSTANCE = new EmptyMultiMap();
    private static final String EMPTY_MULTI_MAP_IS_IMMUTABLE = "EmptyMultiMap is immutable";

    /**
     * Get the total number of key-value pairs in the map.
     *
     * @return the total number of key-value pairs in the map
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * Whether the map is empty.
     *
     * @return whether the map is empty
     */
    @Override
    public boolean isEmpty() {
        return true;
    }

    /**
     * Whether the map contains the specified key.
     *
     * @param o key
     * @return whether the map contains the specified key
     */
    @Override
    public boolean containsKey(Object o) {
        return false;
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
        throw new UnsupportedOperationException(EMPTY_MULTI_MAP_IS_IMMUTABLE);
    }

    /**
     * Put all key-value pairs in the specified map into the map.
     *
     * @param k key
     * @param iterable values
     */
    @Override
    public void putAll(K k, Iterable<? extends V> iterable) {
        throw new UnsupportedOperationException(EMPTY_MULTI_MAP_IS_IMMUTABLE);
    }

    /**
     * Put all key-value pairs in the specified map into the map.
     *
     * @param multiMap map
     */
    @Override
    public void putAll(MultiMap<K, V> multiMap) {
        throw new UnsupportedOperationException(EMPTY_MULTI_MAP_IS_IMMUTABLE);
    }

    /**
     * Remove all key-value pairs with the specified key from the map.
     *
     * @param o key
     * @return all values of the specified key
     */
    @Override
    public Collection<V> removeAll(Object o) {
        throw new UnsupportedOperationException(EMPTY_MULTI_MAP_IS_IMMUTABLE);
    }

    /**
     * Clear the map and remove all key-value pairs.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(EMPTY_MULTI_MAP_IS_IMMUTABLE);
    }

    /**
     * Get all values of the specified key, if the key does not exist, return an empty collection.
     *
     * @param k key
     * @return all values of the specified key
     */
    @Override
    public Collection<V> get(K k) {
        return Collections.emptyList();
    }

    /**
     * Get all keys in the map.
     *
     * @return all keys in the map
     */
    @Override
    public Set<K> keySet() {
        return Collections.emptySet();
    }

    /**
     * Get all values in the map.
     *
     * @return all values in the map
     */
    @Override
    public Collection<V> values() {
        return Collections.emptyList();
    }

    /**
     * <p>Get all key-value pairs in the map.<br />
     * The returned entries are modifiable, but the modification will not affect the map.
     *
     * @return all key-value pairs in the map
     */
    @Override
    public Collection<Map.Entry<K, V>> entries() {
        return Collections.emptyList();
    }

    /**
     * Get java map of the multimap, the returned map is modifiable.
     *
     * @return java map of the multi map
     */
    @Override
    public Map<K, Collection<V>> asMap() {
        return Collections.emptyMap();
    }
}
