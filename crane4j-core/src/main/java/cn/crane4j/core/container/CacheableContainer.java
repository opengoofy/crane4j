package cn.crane4j.core.container;

import cn.crane4j.core.cache.Cache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 具备缓存功能的数据源容器包装类。当根据key集合获取数据源时，将会优先尝试从缓存中获取，
 * 若有部分key集合在缓存中不存在，则会再从原始容器中获取，并为这批key添加缓存。
 *
 * @author huangchengxing
 * @see Cache
 */
@Getter
@RequiredArgsConstructor
public class CacheableContainer<K> implements Container<K> {

    private final Container<K> container;
    private final Cache<K> cache;

    /**
     * 获取数据源容器的命名空间，该值应当全局唯一
     *
     * @return 命名空间
     */
    @Override
    public String getNamespace() {
        return container.getNamespace();
    }

    /**
     * 输入一批key值，返回按key值分组的数据源对象
     *
     * @param keys keys
     * @return 按key值分组的数据源对象
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
