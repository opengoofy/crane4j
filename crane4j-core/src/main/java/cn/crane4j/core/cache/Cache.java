package cn.crane4j.core.cache;

import java.util.Map;

/**
 * Cache object, represents a cache container isolated by name in {@link CacheManager}.
 *
 * @author huangchengxing
 * @param <K> key type
 * @see CacheManager
 */
public interface Cache<K> {

    /**
     * Whether the current cache has expired.
     *
     * @return {@code true} if this cache has expired, otherwise returns {@code false}
     */
    boolean isExpired();

    /**
     * Get the cache according to the key value.
     *
     * @param key key
     * @return cache value
     */
    Object get(K key);

    /**
     * Get all cache according to the key values.
     *
     * @param keys keys
     * @return cache value
     */
    Map<K, Object> getAll(Iterable<K> keys);

    /**
     * Add cache value.
     *
     * @param key key
     * @param value value
     */
    void put(K key, Object value);

    /**
     * Add all cache value.
     *
     * @param caches caches
     */
    void putAll(Map<K, Object> caches);

    /**
     * Add cache value if it does not exist.
     *
     * @param key        key
     * @param cacheValue cache value
     */
    void putIfAbsent(K key, Object cacheValue);
}
