package cn.crane4j.core.container;

import cn.crane4j.core.cache.Cache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * <p>Data source container wrapper class with cache function.<br />
 * When the data source is obtained from the key set,
 * it will first try to obtain it from the cache.<br />
 * If some key sets do not exist in the cache,
 * it will be obtained from the original container and added to the cache for this batch of keys.
 *
 * @author huangchengxing
 * @param <K> key type
 * @see Cache
 */
@Getter
@RequiredArgsConstructor
public class CacheableContainer<K> implements Container<K> {

    private final Container<K> container;
    private final Cache<K> cache;

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
        Map<K, Object> results = new HashMap<>(keys.size());
        List<K> noneCachedKeys = new ArrayList<>(keys.size());
        for (K key : keys) {
            Object val = cache.get(key);
            if (Objects.isNull(val)) {
                noneCachedKeys.add(key);
            } else {
                results.put(key, val);
            }
        }
        Map<K, Object> data = (Map<K, Object>)container.get(noneCachedKeys);
        data.forEach(cache::putIfAbsent);
        results.putAll(data);
        return results;
    }
}
