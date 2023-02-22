package cn.crane4j.core.cache;

import cn.crane4j.core.container.CacheableContainer;

/**
 * Cache object, represents a cache container isolated by name in {@link CacheManager}.
 *
 * @author huangchengxing
 * @see CacheManager
 * @see ConcurrentMapCache
 * @see CacheableContainer
 */
public interface Cache<K> {

    /**
     * Get the cache according to the key value.
     *
     * @param key key
     * @return cache value
     */
    Object get(K key);

    /**
     * Add cache value.
     *
     * @param key key
     * @param value value
     * @return old value
     */
    Object put(K key, Object value);

    /**
     * Add cache value if it does not exist.
     *
     * @param key        key
     * @param cacheValue cache value
     */
    void putIfAbsent(K key, Object cacheValue);
}
