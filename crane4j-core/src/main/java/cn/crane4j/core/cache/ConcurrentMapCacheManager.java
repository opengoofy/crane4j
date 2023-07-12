package cn.crane4j.core.cache;

import cn.crane4j.core.util.CollectionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Simple implementation of {@link CacheManager} based on {@link ConcurrentHashMap}.
 *
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public class ConcurrentMapCacheManager implements CacheManager {

    /**
     * Cache object map
     */
    private final ConcurrentMap<String, CacheImpl<?>> caches = new ConcurrentHashMap<>(8);

    /**
     * cache factory
     */
    private final Supplier<ConcurrentMap<Object, Object>> mapFactory;

    /**
     * <p>Delete the corresponding cache if it already exists.<br />
     * The {@link Cache#isExpired()} of a deleted cache object must return false.
     *
     * @param cacheName cache name
     */
    @Override
    public void removeCache(String cacheName) {
        caches.compute(cacheName, (name, cache) -> {
            if (Objects.nonNull(cache)) {
                log.debug("remove cache [{}]", cacheName);
                cache.setExpired(true);
                cache.cacheMap.clear();
            }
            return null;
        });
    }

    /**
     * <p>Get cache, if it does not exist create it first.<br />
     * The obtained cache is <b>not always</b> guaranteed to be valid,
     * caller needs to ensure the timeliness of the cache itself through {@link Cache#isExpired()}.
     *
     * @param cacheName cache name
     * @param <K> key type
     * @return cache object
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K> Cache<K> getCache(String cacheName) {
        return (Cache<K>) CollectionUtils.computeIfAbsent(caches, cacheName, n -> {
            ConcurrentMap<K, Object> map = (ConcurrentMap<K, Object>)mapFactory.get();
            log.debug("create cache [{}]", cacheName);
            return new CacheImpl<>(map);
        });
    }

    /**
     * Cache impl.
     *
     * @author huangchengxing
     * @param <K> key type
     */
    @RequiredArgsConstructor
    private static class CacheImpl<K> implements Cache<K> {

        @Setter
        @Getter
        private boolean expired = false;

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
         * Get all cache according to the key values.
         *
         * @param keys keys
         * @return cache value
         */
        @Override
        public Map<K, Object> getAll(Iterable<K> keys) {
            if (CollectionUtils.isEmpty(keys)) {
                return Collections.emptyMap();
            }
            Map<K, Object> result = new HashMap<>(16);
            keys.forEach(k -> {
                Object v = cacheMap.get(k);
                if (Objects.nonNull(v)) {
                    result.put(k, v);
                }
            });
            return result;
        }

        /**
         * Add cache value.
         *
         * @param key key
         * @param value value
         */
        @Override
        public void put(K key, Object value) {
            cacheMap.put(key, value);
        }

        /**
         * Add cache value.
         *
         * @param caches caches
         */
        @Override
        public void putAll(Map<K, Object> caches) {
            cacheMap.putAll(caches);
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
}
