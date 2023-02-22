package cn.crane4j.core.cache;

import cn.crane4j.core.container.CacheableContainer;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentMap;

/**
 * <p>The simple implementation of {@link Cache},
 * based on the local cache by {@link ConcurrentMap}.
 * Used to cooperate with {@link CacheableContainer}
 * to cache the data source in the container.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class ConcurrentMapCache<K> implements Cache<K> {

    /**
     * cache map
     */
    private final ConcurrentMap<K, Object> cacheMap;

    /**
     * Get the cache according to the key value.
     *
     * @param key key
     * @return cache value
     */
    @Override
    public Object get(K key) {
        return cacheMap.get(key);
    }

    /**
     * Add cache value.
     *
     * @param key key
     * @param value value
     * @return old value
     */
    @Override
    public Object put(K key, Object value) {
        return cacheMap.put(key, value);
    }

    /**
     * Add cache value if it does not exist.
     *
     * @param key        key
     * @param cacheValue cache value
     */
    @Override
    public void putIfAbsent(K key, Object cacheValue) {
        cacheMap.putIfAbsent(key, cacheValue);
    }
}
