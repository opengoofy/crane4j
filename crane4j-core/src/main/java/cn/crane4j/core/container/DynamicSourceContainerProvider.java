package cn.crane4j.core.container;

import cn.crane4j.annotation.ProvideData;
import cn.crane4j.core.support.DataProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>The implementation class of {@link ContainerProvider}.<br />
 * Provide a container that supports obtaining data sources from thread shared contexts,
 * allowing users to dynamically update the context before the operation is executed,
 * thereby causing the container to return user specified data.
 *
 * @author huangchengxing
 * @see ProvideData
 * @see DynamicSourceContainerProvider
 */
public class DynamicSourceContainerProvider implements ContainerProvider {

    /**
     * context
     */
    private final ThreadLocal<Map<String, DataProvider<?, ?>>> context = new ThreadLocal<>();

    /**
     * Get data source container.
     *
     * @param namespace namespace
     * @return container
     */
    @Override
    public Container<?> getContainer(String namespace) {
        return new DynamicSourceContainer<>(namespace, this);
    }

    /**
     * Clear container data provider in context of current thread.
     *
     * @param namespace namespace
     * @return old container data provider in context of current thread
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <K, V> DataProvider<K, V> removeDataProvider(String namespace) {
        Map<String, DataProvider<?, ?>> table = getDataProviders();
        return (DataProvider<K, V>)table.remove(namespace);
    }

    /**
     * Set container data provider in context of current thread.
     *
     * @param namespace namespace
     * @param dataProvider new data provider
     * @return old container data provider in context of current thread
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <K, V> DataProvider<K, V> setDataProvider(String namespace, DataProvider<?, ?> dataProvider) {
        Map<String, DataProvider<?, ?>> table = getDataProviders();
        DataProvider<?, ?> old = table.remove(namespace);
        table.put(namespace, dataProvider);
        return (DataProvider<K, V>)old;
    }

    /**
     * Get container data provider in context of current thread.
     *
     * @param namespace namespace
     * @return old container data provider in context of current thread
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public <K, V> DataProvider<K, V> getDataProvider(String namespace) {
        Map<String, DataProvider<?, ?>> table =  getDataProviders();
        return (DataProvider<K, V>)table.getOrDefault(namespace, DataProvider.empty());
    }

    /**
     * Clear all container data in context of current thread.
     */
    public void clear() {
        context.remove();
    }

    private Map<String, DataProvider<?, ?>> getDataProviders() {
        Map<String, DataProvider<?, ?>> providers = context.get();
        if (Objects.isNull(providers)) {
            providers = new HashMap<>(8);
            context.set(providers);
        }
        return providers;
    }

    /**
     * Dynamic source container.
     *
     * @author huancghengxing
     */
    @RequiredArgsConstructor
    private static class DynamicSourceContainer<K> implements Container<K> {

        @Getter
        private final String namespace;
        private final DynamicSourceContainerProvider provider;

        /**
         * Enter a batch of key values to return data source objects grouped by key values.
         *
         * @param keys keys
         * @return data source objects grouped by key value
         */
        @Override
        public Map<K, ?> get(Collection<K> keys) {
            return provider.<K, Object>getDataProvider(namespace).apply(keys);
        }
    }
}
