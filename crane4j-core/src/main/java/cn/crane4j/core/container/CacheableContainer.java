package cn.crane4j.core.container;

import cn.crane4j.core.cache.Cache;
import cn.crane4j.core.cache.CacheManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Data source container wrapper class with cache function.<br />
 * When the data source is obtained from the key set,
 * it will first try to obtain it from the cache.<br />
 * If some key sets do not exist in the cache,
 * it will be obtained from the original container and added to the cache for this batch of keys.
 *
 * @author huangchengxing
 * @param <K> key type
 * @see CacheManager
 * @since 2.0.0
 */
@Getter
@RequiredArgsConstructor
public class CacheableContainer<K> implements Container<K>, Container.Lifecycle {

    private final Container<K> container;
    private final CacheManager cacheManager;
    private final String cacheName;

    /**
     * Gets the namespace of the data source container,
     * always return the namespace of the wrapped original container.
     *
     * @return namespace
     */
    @Override
    public String getNamespace() {
        return container.getNamespace();
    }

    /**
     * <p>Enter a batch of key values to return data source objects grouped by key values.
     * If part of the key value entered has been cached,
     * the data corresponding to that part of the key value will be obtained from the cache first.
     *
     * @param keys keys
     * @return data source objects grouped by key value
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<K, ?> get(Collection<K> keys) {
        Cache<K> cache = cacheManager.getCache(cacheName);
        Map<K, Object> cachedValues = cache.getAll(keys);
        Set<K> noneCachedKeys = keys.stream().filter(k -> !cachedValues.containsKey(k)).collect(Collectors.toSet());
        Map<K, Object> noneCachedValues = noneCachedKeys.isEmpty() ?
            Collections.emptyMap() : (Map<K, Object>)container.get(noneCachedKeys);
        if (!cache.isExpired()) {
            cache.putAll(noneCachedValues);
        }
        cachedValues.putAll(noneCachedValues);
        return cachedValues;
    }

    /**
     * Initialize the container
     */
    @Override
    public void init() {
        if (container instanceof Container.Lifecycle) {
            ((Container.Lifecycle)container).init();
        }
    }

    /**
     * Destroy the container
     */
    @Override
    public void destroy() {
        if (container instanceof Container.Lifecycle) {
            ((Container.Lifecycle)container).destroy();
        }
    }
}
