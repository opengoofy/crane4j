package cn.crane4j.core.container;

import cn.crane4j.core.support.DataProvider;
import cn.crane4j.core.util.ReadWriteLockSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A shared context.
 *
 * @author huangchengxing
 */
public class SharedContextContainerProvider implements DynamicSourceContainerProvider {

    private final Map<String, DataProvider<?, ?>> context = new HashMap<>(8);
    private final ReadWriteLockSupport readWriteLockSupport = new ReadWriteLockSupport(new ReentrantReadWriteLock());

    /**
     * Clear container data provider in context of current thread.
     *
     * @param namespace namespace
     * @return old container data provider in context of current thread
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <K, V> DataProvider<K, V> removeDataProvider(String namespace) {
        return (DataProvider<K, V>)readWriteLockSupport.withWriteLock(() -> context.remove(namespace));
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
    @Override
    public <K, V> DataProvider<K, V> setDataProvider(String namespace, DataProvider<?, ?> dataProvider) {
        return (DataProvider<K, V>)readWriteLockSupport.withWriteLock(() -> context.put(namespace, dataProvider));
    }

    /**
     * Get container data provider in context of current thread.
     *
     * @param namespace namespace
     * @return old container data provider in context of current thread
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <K, V> DataProvider<K, V> getDataProvider(String namespace) {
        return readWriteLockSupport.withReadLock(() -> (DataProvider<K, V>)context.getOrDefault(namespace, DataProvider.empty()));
    }

    /**
     * Clear all container data in context of current thread.
     */
    @Override
    public void clear() {
        readWriteLockSupport.withWriteLock(context::clear);
    }
}
