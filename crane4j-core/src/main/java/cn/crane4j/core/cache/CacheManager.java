package cn.crane4j.core.cache;

/**
 * Cache manager.
 *
 * @author huangchengxing
 * @see Cache
 * @see SimpleCacheManager
 */
public interface CacheManager {

    /**
     * Get cache, if it does not exist create it first.
     *
     * @param cacheName cache name
     * @return cache object
     */
    <K> Cache<K> getCache(String cacheName);
}
