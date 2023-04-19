package cn.crane4j.core.container;

import cn.crane4j.core.support.DataProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * <p>The implementation class of {@link ContainerProvider}.<br />
 * Provide a container that supports obtaining data sources from thread shared contexts,
 * allowing users to dynamically update the context before the operation is executed,
 * thereby causing the container to return user specified data.
 *
 * @author huangchengxing
 * @see SharedContextContainerProvider
 * @see ThreadContextContainerProvider
 */
public interface DynamicSourceContainerProvider extends ContainerProvider {

    /**
     * Get data source container.
     *
     * @param namespace namespace
     * @return container
     */
    @Override
    default Container<?> getContainer(String namespace) {
        return new DynamicSourceContainer<>(namespace, this);
    }

    /**
     * Clear container data provider in context.
     *
     * @param namespace namespace
     * @return old container data provider in context
     */
    @Nullable
    <K, V> DataProvider<K, V> removeDataProvider(String namespace);

    /**
     * Set container data provider in context.
     *
     * @param namespace namespace
     * @param dataProvider new data provider
     * @return old container data provider in context
     */
    @Nullable
    <K, V> DataProvider<K, V> setDataProvider(String namespace, DataProvider<?, ?> dataProvider);

    /**
     * Get container data provider in context.
     *
     * @param namespace namespace
     * @return old container data provider in context
     */
    @Nonnull
    <K, V> DataProvider<K, V> getDataProvider(String namespace);

    /**
     * Clear all container data in context.
     */
    void clear();

    /**
     * Dynamic source container.
     *
     * @author huancghengxing
     */
    @RequiredArgsConstructor
    class DynamicSourceContainer<K> implements Container<K> {

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
