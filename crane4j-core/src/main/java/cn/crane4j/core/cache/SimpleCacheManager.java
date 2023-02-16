package cn.crane4j.core.cache;

import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * 简单缓存管理器
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class SimpleCacheManager implements CacheManager {

    /**
     * 缓存对象
     */
    private final ConcurrentMap<String, Cache<?>> caches;

    /**
     * 缓存工厂
     */
    private final Function<String, Cache<?>> cacheFactory;

    /**
     * 获取缓存，若不存在则创建缓存
     *
     * @param cacheName    缓存名
     * @return 缓存对象
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K> Cache<K> getCache(String cacheName) {
        return (Cache<K>) MapUtil.computeIfAbsent(caches, cacheName, cacheFactory);
    }
}
