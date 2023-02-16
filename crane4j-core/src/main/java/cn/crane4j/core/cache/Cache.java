package cn.crane4j.core.cache;

import cn.crane4j.core.container.CacheableContainer;

import java.util.concurrent.ConcurrentMap;

/**
 * {@link Cache}接口的简单实现，基于{@link ConcurrentMap}实现的本地缓存。
 * 一般用于配合{@link CacheableContainer}实现对容器中数据源的缓存。
 *
 * @author huangchengxing
 * @see CacheManager
 * @see ConcurrentMapCache
 * @see CacheableContainer
 */
public interface Cache<K> {

    /**
     * 根据key值获取缓存
     *
     * @param key key
     * @return 缓存值
     */
    Object get(K key);

    /**
     * 添加缓存值
     *
     * @param key key
     * @param value value
     * @return 若已有缓存值则添加
     */
    Object put(K key, Object value);

    /**
     * 若不存在则添加缓存值
     *
     * @param key        key
     * @param cacheValue 缓存值
     */
    void putIfAbsent(K key, Object cacheValue);
}
