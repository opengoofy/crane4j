package cn.crane4j.core.cache;

/**
 * Cache manager.
 *
 * @author huangchengxing
 * @see Cache
 * @see ConcurrentMapCacheManager
 */
public interface CacheManager {

    /**
     * <p>Delete the corresponding cache if it already exists.<br />
     * The {@link Cache#isExpired()} of a deleted cache object must return false.
     *
     * @param cacheName cache name
     */
    void removeCache(String cacheName);

    /**
     * <p>Get cache, if it does not exist create it first.<br />
     * The obtained cache is <b>not always</b> guaranteed to be valid,
     * caller needs to ensure the timeliness of the cache itself through {@link Cache#isExpired()}.
     *
     * @param cacheName cache name
     * @param <K> key type
     * @return cache object
     */
    <K> Cache<K> getCache(String cacheName);
}
