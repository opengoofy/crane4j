package cn.crane4j.core.cache;

import cn.crane4j.core.util.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A cache object that provides basic cache operations.
 *
 * @author huangchengxing
 * @param <K> key type
 * @see CacheableContainer
 * @see CacheManager
 * @since 2.4.0
 */
public interface CacheObject<K> {

    /**
     * Get the name of this cache.
     *
     * @return cache name
     */
    String getName();

    /**
     * Whether the cache is expired.
     *
     * @return expire time
     * @see CacheManager#removeCache
     * @see CacheManager#createCache
     */
    boolean isInvalid();

    /**
     * Get the cache according to the key value.
     *
     * @param key key
     * @return cache value
     */
    @Nullable
    Object get(K key);

    /**
     * Get all cache according to the key values.
     *
     * @param keys keys
     * @return cache value
     */
    default Map<K, Object> getAll(Iterable<K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        }
        Map<K, Object> results = new HashMap<>(16);
        keys.forEach(key -> {
            Object value = get(key);
            if (Objects.nonNull(value)) {
                results.put(key, value);
            }
        });
        return results;
    }

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
    default void putAll(Map<K, Object> caches) {
        if (CollectionUtils.isEmpty(caches)) {
            return;
        }
        caches.forEach(this::put);
    }

    /**
     * Add cache value if it does not exist.
     *
     * @param key key
     * @param cacheValue cache value
     */
    void putIfAbsent(K key, Object cacheValue);

    /**
     * Remove cache value.
     *
     * @param key key
     */
    void remove(K key);

    /**
     * Remove all cache value.
     *
     * @param keys keys
     */
    default void removeAll(Iterable<K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        keys.forEach(this::remove);
    }

    /**
     * Clear all cache value.
     */
    void clear();
}
