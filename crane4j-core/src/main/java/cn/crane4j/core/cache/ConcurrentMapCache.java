package cn.crane4j.core.cache;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentMap;

/**
 * 基于{@link ConcurrentMap}实现的简单缓存
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class ConcurrentMapCache<K> implements Cache<K> {

    /**
     * 缓存集合
     */
    private final ConcurrentMap<K, Object> cacheMap;

    /**
     * 根据key值获取缓存
     *
     * @param key key
     * @return 缓存值
     */
    @Override
    public Object get(K key) {
        return cacheMap.get(key);
    }

    /**
     * 添加缓存值
     *
     * @param key   key
     * @param value value
     * @return 若已有缓存值则添加
     */
    @Override
    public Object put(K key, Object value) {
        return cacheMap.put(key, value);
    }

    /**
     * 若不存在则添加缓存值
     *
     * @param key        key
     * @param cacheValue 缓存值
     */
    @Override
    public void putIfAbsent(K key, Object cacheValue) {
        cacheMap.putIfAbsent(key, cacheValue);
    }
}
