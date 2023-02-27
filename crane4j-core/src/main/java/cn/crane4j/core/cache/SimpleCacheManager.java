package cn.crane4j.core.cache;

import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Simple implementation of {@link CacheManager}.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class SimpleCacheManager implements CacheManager {

    /**
     * Cache object map
     */
    private final ConcurrentMap<String, Cache<?>> caches;

    /**
     * cache factory
     */
    private final Function<String, Cache<?>> cacheFactory;

    /**
     * Get cache, if it does not exist create it first.
     *
     * @param cacheName cache name
     * @param <K> key type
     * @return cache object
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K> Cache<K> getCache(String cacheName) {
        return (Cache<K>) MapUtil.computeIfAbsent(caches, cacheName, cacheFactory);
    }
}
