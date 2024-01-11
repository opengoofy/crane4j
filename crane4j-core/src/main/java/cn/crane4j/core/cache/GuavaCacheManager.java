package cn.crane4j.core.cache;

import cn.crane4j.core.util.Asserts;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * An implementation of the {@link CacheManager} that
 * creates a cache instance what stores data in the {@link Cache}.
 *
 * @author huangchengxing
 * @since 2.4.0
 */
@NoArgsConstructor
public class GuavaCacheManager extends AbstractCacheManager {

    /**
     * The cache factory.
     */
    @NonNull
    @Setter
    private CacheFactory cacheFactory = DefaultCacheFactory.INSTANCE;

    /**
     * Create cache instance.
     *
     * @param name cache name
     * @param expireTime expire time
     * @param timeUnit   time unit
     * @return cache instance
     */
    @NonNull
    protected <K> GuavaCacheObject<K> doCreateCache(String name, Long expireTime, TimeUnit timeUnit) {
        Cache<Object, Object> cache = cacheFactory.getCache(expireTime, timeUnit);
        Asserts.isNotNull(cache, "Cache factory must not be null");
        return new GuavaCacheObject<>(name, cache);
    }

    /**
     * A factory to create a cache instance.
     *
     * @see CacheBuilder
     */
    @FunctionalInterface
    public interface CacheFactory {

        /**
         * Get the cache instance.
         *
         * @param expireTime expire time
         * @param timeUnit  time unit
         * @return guava cache instance
         */
        Cache<Object, Object> getCache(Long expireTime, TimeUnit timeUnit);
    }

    /**
     * A default {@link CacheFactory} implementation,
     * if expire time greater than 0, use {@link CacheBuilder#expireAfterWrite(long, TimeUnit)},
     * if expire time less than 0, use {@link CacheBuilder#weakKeys()} and {@link CacheBuilder#weakValues()}.
     *
     * @author huangchengxing
     */
    public static class DefaultCacheFactory implements CacheFactory {

        public static final DefaultCacheFactory INSTANCE = new DefaultCacheFactory();

        /**
         * Get the cache instance.
         *
         * @param expireTime expire time
         * @param timeUnit   time unit
         * @return guava cache instance
         */
        @Override
        public Cache<Object, Object> getCache(Long expireTime, TimeUnit timeUnit) {
            Asserts.isNotEquals(expireTime, 0L, "Expire time must not be 0");
            if (expireTime > 1) {
                return CacheBuilder.newBuilder()
                    .expireAfterWrite(expireTime, timeUnit)
                    .build();
            }
            // if expire time less than 0, use weak keys and weak values
            return CacheBuilder.newBuilder()
                .weakKeys().weakValues()
                .build();
        }
    }

    /**
     * A {@link CacheObject} implementation that stores data in the {@link Cache}.
     *
     * @author huangchengxing
     * @since 2.4.0
     */
    private static class GuavaCacheObject<K> extends AbstractCacheObject<K> {

        private final Cache<Object, Object> cache;

        public GuavaCacheObject(String name, Cache<Object, Object> cache) {
            super(name);
            this.cache = cache;
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
            return cache.getIfPresent(key);
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

        /**
         * Remove cache value.
         *
         * @param key key
         */
        @Override
        public void remove(K key) {
            cache.invalidate(key);
        }

        /**
         * Clear all cache value.
         */
        @Override
        public void clear() {
            cache.invalidateAll();
        }
    }
}
