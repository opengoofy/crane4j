package cn.crane4j.core.cache;

import cn.hutool.core.map.MapUtil;
import com.google.common.cache.LoadingCache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Simple implementation of {@link CacheManager} based on {@link LoadingCache}.
 *
 * @author huangchengxing
 */
@Slf4j
@RequiredArgsConstructor
public class GuavaCacheManager implements CacheManager {

    private final ConcurrentMap<String, CacheImpl<?>> caches = new ConcurrentHashMap<>(8);
    private final Supplier<com.google.common.cache.Cache<Object, ?>> cacheFactory;

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
                log.info("remove cache [{}]", cacheName);
                cache.setExpired(true);
                cache.cache.asMap().clear();
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
     * @return cache object
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K> Cache<K> getCache(String cacheName) {
        return (Cache<K>) MapUtil.computeIfAbsent(caches, cacheName, n -> {
            com.google.common.cache.Cache<K, Object> cache = (com.google.common.cache.Cache<K, Object>)cacheFactory.get();
            log.info("create cache [{}]", cacheName);
            return new CacheImpl<>(cache);
        });
    }

    @RequiredArgsConstructor
    private static class CacheImpl<K> implements Cache<K> {

        @Setter
        @Getter
        private boolean expired = false;
        private final com.google.common.cache.Cache<K, Object> cache;

        /**
         * Get the cache according to the key value.
         *
         * @param key key
         * @return cache value
         */
        @SneakyThrows
        @Override
        public Object get(K key) {
            return cache.get(key, () -> null);
        }

        /**
         * Get all cache according to the key values.
         *
         * @param keys keys
         * @return cache value
         */
        @Override
        public Map<K, Object> getAll(Iterable<K> keys) {
            return cache.getAllPresent(keys);
        }

        /**
         * Add cache value.
         *
         * @param key   key
         * @param value value
         */
        @Override
        public void put(K key, Object value) {
            cache.put(key, value);
        }

        /**
         * Add all cache value.
         *
         * @param caches caches
         */
        @Override
        public void putAll(Map<K, Object> caches) {
            cache.putAll(caches);
        }

        /**
         * Add cache value if it does not exist.
         *
         * @param key        key
         * @param cacheValue cache value
         */
        @SneakyThrows
        @Override
        public void putIfAbsent(K key, Object cacheValue) {
            cache.get(key, () -> cacheValue);
        }
    }
}
