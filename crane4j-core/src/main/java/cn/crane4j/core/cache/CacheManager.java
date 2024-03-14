package cn.crane4j.core.cache;

import cn.crane4j.core.support.NamedComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * {@link CacheObject} manager.
 *
 * @author huangchengxing
 * @see CacheObject
 * @see GuavaCacheManager
 * @see MapCacheManager#newWeakConcurrentMapCacheManager()
 * @see MapCacheManager#newConcurrentHashMapCacheManager()
 * @since 2.4.0
 */
public interface CacheManager extends NamedComponent {

    String DEFAULT_MAP_CACHE_MANAGER_NAME = "MapCacheManager";
    String DEFAULT_GUAVA_CACHE_MANAGER_NAME = "GuavaCacheManager";

    /**
     * Create cache instance, if cache instance already created,
     * remove the old cache instance and create a new cache instance.
     *
     * @param name cache name
     * @param expireTime expire time
     * @param timeUnit time unit
     * @param <K> key type
     * @return cache instance
     */
    @NonNull
    <K> CacheObject<K> createCache(String name, Long expireTime, TimeUnit timeUnit);

    /**
     * Get cache instance by name,
     * if cache instance still not created by {@link #createCache}, return null.
     *
     * @param name cache name
     * @return cache instance
     */
    @Nullable
    <K> CacheObject<K> getCache(String name);

    /**
     * <p>Remove cache.<br />
     * When a cache is removed, manager will call {@link CacheObject#clear()} to clear the cache,
     * and mark the cache as invalid by {@link CacheObject#isInvalid()}.
     *
     * @param name cache name
     * @see CacheObject#isInvalid()
     * @see CacheObject#clear()
     */
    void removeCache(String name);

    /**
     * Clear all cache.
     */
    void clearAll();
}
