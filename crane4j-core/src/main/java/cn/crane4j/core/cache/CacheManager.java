package cn.crane4j.core.cache;

/**
 * 缓存管理器
 *
 * @author huangchengxing
 * @see Cache
 * @see SimpleCacheManager
 */
public interface CacheManager {

    /**
     * 获取缓存，若不存在则创建缓存
     *
     * @param cacheName 缓存名
     * @return 缓存对象
     */
    <K> Cache<K> getCache(String cacheName);
}
