package cn.crane4j.core.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Abstract cache manager.
 *
 * @author huangchengxing
 * @since 2.4.0
 */
public abstract class AbstractCacheManager implements CacheManager {

    private final ConcurrentMap<String, AbstractCacheObject<?>> caches = new ConcurrentHashMap<>();

    /**
     * Get cache instance by name,
     * if cache instance still not created by {@link #createCache}, return null.
     *
     * @param name cache name
     * @return cache instance
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <K> CacheObject<K> getCache(String name) {
        return (CacheObject<K>) caches.get(name);
    }

    /**
     * Create cache instance, if cache instance already created,
     * remove the old cache instance and create a new cache instance.
     *
     * @param name       cache name
     * @param expireTime expire time
     * @param timeUnit   time unit
     * @return cache instance
     */
    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <K> CacheObject<K> createCache(
        String name, Long expireTime, TimeUnit timeUnit) {
        AbstractCacheObject<Object> cacheObject = doCreateCache(name, expireTime, timeUnit);
        AbstractCacheObject<?> old = caches.put(name, cacheObject);
        if (Objects.nonNull(old)) {
            invalidate(old);
        }
        return (CacheObject<K>)cacheObject;
    }


    /**
     * Remove cache.
     *
     * @param name cache name
     * @see CacheObject#isInvalid()
     * @see CacheObject#clear()
     */
    @Override
    public void removeCache(String name) {
        caches.computeIfPresent(name, (k, v) -> {
            invalidate(v);
            return null;
        });
    }

    /**
     * Clear all cache.
     */
    @Override
    public void clearAll() {
        List<AbstractCacheObject<?>> allCaches = new ArrayList<>(caches.values());
        caches.clear();
        allCaches.forEach(this::invalidate);
    }

    /**
     * Create cache instance.
     *
     * @param name cache name
     * @param expireTime expire time
     * @param timeUnit   time unit
     * @return cache instance
     */
    @NonNull
    protected abstract <K> AbstractCacheObject<K> doCreateCache(String name, Long expireTime, TimeUnit timeUnit);

    /**
     * Invalidate cache.
     *
     * @param cacheObject cache object
     */
    protected void invalidate(AbstractCacheObject<?> cacheObject) {
        cacheObject.setInvalid(true);
        cacheObject.clear();
    }

    /**
     * Abstract cache object.
     *
     * @param <K> key type
     * @author huangchengxing
     * @since 2.4.0
     */
    @Getter
    @RequiredArgsConstructor
    protected abstract static class AbstractCacheObject<K> implements CacheObject<K> {
        @Setter
        private volatile boolean invalid = false;
        private final String name;
    }
}
