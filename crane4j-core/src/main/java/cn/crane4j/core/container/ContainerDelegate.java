package cn.crane4j.core.container;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Map;

/**
 * A delegate container that can be used to wrap another container.
 *
 * @author huangchengxing
 * @since 2.6.0
 */
public interface ContainerDelegate<K> extends Container<K>, Container.Lifecycle {

    /**
     * Get the delegate container.
     *
     * @return delegate container
     */
    @NonNull
    Container<K> getContainer();

    /**
     * Gets the namespace of the data source container,
     * which should be globally unique.
     *
     * @return namespace
     */
    @Override
    default String getNamespace() {
        return getContainer().getNamespace();
    }

    /**
     * Enter a batch of key values to return data source objects grouped by key values.
     *
     * @param keys keys
     * @return data source objects grouped by key value
     */
    @Override
    default Map<K, ?> get(Collection<K> keys) {
        return getContainer().get(keys);
    }

    /**
     * Initialize the container
     */
    @Override
    default void init() {
        Container<?> container = getContainer();
        if (container instanceof Container.Lifecycle) {
            ((Container.Lifecycle)container).init();
        }
    }

    /**
     * Destroy the container
     */
    @Override
    default void destroy() {
        Container<?> container = getContainer();
        if (container instanceof Container.Lifecycle) {
            ((Container.Lifecycle)container).destroy();
        }
    }
}
