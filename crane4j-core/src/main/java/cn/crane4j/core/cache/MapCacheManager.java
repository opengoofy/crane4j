package cn.crane4j.core.cache;

import cn.crane4j.core.util.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of the {@link CacheManager} that
 * creates a cache instance what stores data in the {@link Map}.
 *
 * @author huangchengxing
 * @since 2.4.0
 */
public abstract class MapCacheManager extends AbstractCacheManager {

    /**
     * Create a {@link ConcurrentHashMapCacheManager} instance.
     *
     * @return {@link ConcurrentHashMapCacheManager} instance
     */
    public static MapCacheManager newConcurrentHashMapCacheManager() {
        return new ConcurrentHashMapCacheManager();
    }

    /**
     * Create a {@link WeakConcurrentMapCacheManager} instance.
     *
     * @return {@link WeakConcurrentMapCacheManager} instance
     */
    public static MapCacheManager newWeakConcurrentMapCacheManager() {
        return new WeakConcurrentMapCacheManager();
    }

    /**
     * Create cache instance.
     *
     * @param name cache name
     * @param expireTime expire time
     * @param timeUnit   time unit
     * @return cache instance
     */
    @Override
    @NonNull
    protected <K> MapCacheObject<K> doCreateCache(String name, Long expireTime, TimeUnit timeUnit) {
        return new MapCacheObject<>(name, createMap());
    }

    /**
     * Create a {@link Map} instance.
     *
     * @param <K> key type
     * @return map instance
     */
    protected abstract <K> Map<K, Object> createMap();

    /**
     * An implementation of the {@link CacheObject} that stores data in the {@link Map}.
     *
     * @author huangchengxing
     * @since 2.4.0
     */
    protected static class MapCacheObject<K> extends AbstractCacheObject<K> {

        private final Map<K, Object> map;

        public MapCacheObject(String name, Map<K, Object> map) {
            super(name);
            this.map = map;
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
            return map.get(key);
        }

        /**
         * Add cache value.
         *
         * @param key key
         * @param value value
         */
        @Override
        public void put(K key, Object value) {
            map.put(key, value);
        }

        /**
         * Add cache value if it does not exist.
         *
         * @param key key
         * @param value cache value
         */
        @Override
        public void putIfAbsent(K key, Object value) {
            map.putIfAbsent(key, value);
        }

        /**
         * Remove cache value.
         *
         * @param key key
         */
        @Override
        public void remove(K key) {
            map.remove(key);
        }

        /**
         * Clear all cache value.
         */
        @Override
        public void clear() {
            map.clear();
        }
    }

    /**
     * A {@link CacheManager} that creates a cache instance what stores data in the {@link ConcurrentHashMap}.
     * The storage data will not be automatically cleared,
     * unless remove the cache instance by {@link CacheManager#removeCache(String)}
     * or call {@link CacheObject#clear()} method.
     *
     * @author huangchengxing
     * @since 2.4.0
     */
    public static class ConcurrentHashMapCacheManager extends MapCacheManager {
        @Override
        protected <K> Map<K, Object> createMap() {
            return new ConcurrentHashMap<>(16);
        }
    }

    /**
     * A {@link CacheManager} that creates a cache instance what stores data in the {@link CollectionUtils#newWeakConcurrentMap()}.
     * The storage data will be automatically cleared when the JVM garbage collection is performed.
     *
     * @author huangchengxing
     * @since 2.4.0
     */
    public static class WeakConcurrentMapCacheManager extends MapCacheManager {
        @Override
        protected <K> Map<K, Object> createMap() {
            return CollectionUtils.newWeakConcurrentMap();
        }
    }
}
