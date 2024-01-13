package cn.crane4j.extension.redis;

import cn.crane4j.core.cache.AbstractCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The cache manager based on spring {@link RedisTemplate}.
 *
 * @param <K> key type
 * @param <V> value type
 * @author huangchengxing
 * @see RedisTemplate
 * @see StringKeyRedisCacheManager
 * @see GeneralRedisCacheManager
 * @since 2.4.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRedisCacheManager<K, V> extends AbstractCacheManager {

    @NonNull
    protected final RedisTemplate<K, V> redisTemplate;

    /**
     * Create cache instance.
     *
     * @param name       cache name
     * @param expireTime expire time
     * @param timeUnit   time unit
     * @return cache instance
     */
    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    protected RedisCacheObject doCreateCache(String name, Long expireTime, TimeUnit timeUnit) {
        return new RedisCacheObject(name, expireTime, timeUnit);
    }

    /**
     * Get cache key which is used to store cache value in redis.
     *
     * @param cacheName cache name
     * @param key cache key
     * @return cache key
     */
    protected abstract K resolveCacheKey(String cacheName, K key);

    /**
     * Resolve cache value.
     *
     * @param value cache value
     * @return cache value
     */
    protected abstract V resolveCacheValue(Object value);

    /**
     * Clear all cache value for specified cache object.
     *
     * @param cacheName cache name
     */
    protected void clearCache(String cacheName) {
        log.warn("Clear all cache value is not supported in redis cache [{}]", cacheName);
    }

    /**
     * Execute operation in pipeline.
     *
     * @param consumer consumer
     */
    @SuppressWarnings("unchecked")
    protected void executePipelined(Consumer<RedisOperations<K, V>> consumer) {
        SessionCallback<Object> callback = new SessionCallback<Object>() {
            @Override
            public <U, R> Object execute(@NonNull RedisOperations<U, R> operations) throws DataAccessException {
                RedisOperations<K, V> ops = (RedisOperations<K, V>)operations;
                consumer.accept(ops);
                return null;
            }
        };
        redisTemplate.executePipelined(callback);
    }

    /**
     * Redis cache object.
     *
     * @author huangchengxing
     * @since 2.4.0
     */
    protected class RedisCacheObject extends AbstractCacheObject<K> {

        private final long expireTime;
        private final TimeUnit timeUnit;

        protected RedisCacheObject(String name, long expireTime, TimeUnit timeUnit) {
            super(name);
            this.expireTime = expireTime;
            this.timeUnit = timeUnit;
        }

        /**
         * Clear all cache value.
         */
        @Override
        public void clear() {
            clearCache(getName());
        }

        /**
         * Add all cache value.
         *
         * @param caches caches
         */
        @Override
        public void putAll(Map<K, Object> caches) {
            executePipelined(ops -> {
                for (Map.Entry<K, Object> entry : caches.entrySet()) {
                    K cacheKey = resolveCacheKey(getName(), entry.getKey());
                    V cacheValue = resolveCacheValue(entry.getValue());
                    ops.opsForValue().set(cacheKey, cacheValue, expireTime, timeUnit);
                }
            });
        }

        /**
         * Get all cache according to the key values.
         *
         * @param keys keys
         * @return cache value
         */
        @Override
        public Map<K, Object> getAll(Iterable<K> keys) {
            Set<K> keySet = StreamSupport.stream(keys.spliterator(), false)
                .map(key -> resolveCacheKey(getName(), key))
                .collect(Collectors.toCollection(LinkedHashSet::new));
            List<V> values = redisTemplate.opsForValue().multiGet(keySet);
            if (Objects.isNull(values) || values.isEmpty()) {
                return Collections.emptyMap();
            }
            // merged keys and values
            Map<K, Object> results = new LinkedHashMap<>(16);
            int index = 0;
            for (K key : keys) {
                V value = values.get(index++);
                if (Objects.nonNull(value)) {
                    results.put(key, value);
                }
            }
            return results;
        }

        /**
         * Remove all cache value.
         *
         * @param keys keys
         */
        @Override
        public void removeAll(Iterable<K> keys) {
            Set<K> keySet = StreamSupport.stream(keys.spliterator(), false)
                .map(key -> resolveCacheKey(getName(), key))
                .collect(Collectors.toSet());
            redisTemplate.delete(keySet);
        }

        /**
         * Get the cache according to the key value.
         *
         * @param key key
         * @return cache value
         */
        @Nullable
        @Override
        public Object get(K key) {
            K cacheKey = resolveCacheKey(getName(), key);
            return redisTemplate.opsForValue().get(cacheKey);
        }

        /**
         * Add cache value.
         *
         * @param key   key
         * @param value value
         */
        @Override
        public void put(K key, Object value) {
            K cacheKey = resolveCacheKey(getName(), key);
            V cacheValue = resolveCacheValue(value);
            redisTemplate.opsForValue().set(cacheKey, cacheValue, expireTime, timeUnit);
        }

        /**
         * Add cache value if it does not exist.
         *
         * @param key        key
         * @param value cache value
         */
        @Override
        public void putIfAbsent(K key, Object value) {
            K cacheKey = resolveCacheKey(getName(), key);
            V cacheValue = resolveCacheValue(value);
            redisTemplate.opsForValue()
                .setIfAbsent(cacheKey, cacheValue, expireTime, timeUnit);
        }

        /**
         * Remove cache value.
         *
         * @param key key
         */
        @Override
        public void remove(K key) {
            K cacheKey = resolveCacheKey(getName(), key);
            redisTemplate.delete(cacheKey);
        }
    }
}
