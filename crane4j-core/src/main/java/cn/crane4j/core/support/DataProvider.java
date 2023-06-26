package cn.crane4j.core.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Enter a batch of keys and return the associated values grouped by key.
 *
 * @param <K> key type
 * @param <V> value type
 * @author huangchengxing
 */
@FunctionalInterface
public interface DataProvider<K, V> extends Function<Collection<K>, Map<K, V>> {
    
    /**
     * Get an empty provider that always return {@link Collections#emptyMap()} when call {@link #apply}.
     *
     * @param <K> key type
     * @param <V> value type
     * @return data provider
     */
    static <K, V> DataProvider<K, V> empty() {
        return ids -> Collections.emptyMap();
    }

    /**
     * Get a provider that always return {@code data} when call {@link #apply}.
     *
     * @param data data to return
     * @param <K> key type
     * @param <V> value type
     * @return data provider
     */
    static <K, V> DataProvider<K, V> fixed(Map<K, V> data) {
        return Objects.isNull(data) ?
            ids -> Collections.emptyMap() : ids -> data;
    }
}
