package cn.crane4j.core.container;

import cn.crane4j.core.support.DataProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A thread local context.
 *
 * @author huangchengxing
 */
public class ThreadContextContainerProvider implements DynamicSourceContainerProvider {

    /**
     * context
     */
    private final ThreadLocal<Map<String, DataProvider<?, ?>>> context = new ThreadLocal<>();

    /**
     * Clear container data provider in context.
     *
     * @param namespace namespace
     * @return old container data provider in context
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <K, V> DataProvider<K, V> removeDataProvider(String namespace) {
        Map<String, DataProvider<?, ?>> table = getDataProviders();
        return (DataProvider<K, V>)table.remove(namespace);
    }

    /**
     * Set container data provider in context.
     *
     * @param namespace namespace
     * @param dataProvider new data provider
     * @return old container data provider in context
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <K, V> DataProvider<K, V> setDataProvider(String namespace, DataProvider<?, ?> dataProvider) {
        Map<String, DataProvider<?, ?>> table = getDataProviders();
        DataProvider<?, ?> old = table.remove(namespace);
        table.put(namespace, dataProvider);
        return (DataProvider<K, V>)old;
    }


    /**
     * Get container data provider in context.
     *
     * @param namespace namespace
     * @return old container data provider in context
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <K, V> DataProvider<K, V> getDataProvider(String namespace) {
        Map<String, DataProvider<?, ?>> table = getDataProviders();
        return (DataProvider<K, V>)table.getOrDefault(namespace, DataProvider.empty());
    }

    /**
     * Clear all container data in context.
     */
    @Override
    public void clear() {
        context.remove();
    }

    @Nonnull
    private Map<String, DataProvider<?, ?>> getDataProviders() {
        Map<String, DataProvider<?, ?>> providers = context.get();
        if (Objects.isNull(providers)) {
            providers = new HashMap<>(8);
            context.set(providers);
        }
        return providers;
    }
}
