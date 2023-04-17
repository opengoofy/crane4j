package cn.crane4j.core.container;

import cn.crane4j.annotation.SharedContextContainer;
import cn.crane4j.core.support.DataProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
 * @see SharedContextContainer
 * @see SharedContextContainerProvider
 */
public class SharedContextContainerProvider implements ContainerProvider {

    /**
     * context
     */
    private final ThreadLocal<Map<String, DataProvider<?, ?>>> context = new InheritableThreadLocal<>();

    /**
     * Get data source container.
     *
     * @param namespace namespace
     * @return container
     */
    @Override
    public Container<?> getContainer(String namespace) {
        return LambdaContainer.forLambda(namespace, ids -> getDataProvider(namespace).apply(ids));
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
    public Map<Object, Object> setDataProvider(String namespace, DataProvider<?, ?> dataProvider) {
        Map<String, DataProvider<?, ?>> table = getDataProviders();
        DataProvider<?, ?> old = table.remove(namespace);
        table.put(namespace, dataProvider);
        return (Map<Object, Object>)old;
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
            providers = new HashMap<>();
            context.set(providers);
        }
        return providers;
    }
}
