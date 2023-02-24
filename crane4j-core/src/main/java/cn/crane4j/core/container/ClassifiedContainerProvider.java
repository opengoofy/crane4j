package cn.crane4j.core.container;

import cn.crane4j.core.exception.Crane4jException;
import cn.hutool.core.lang.Assert;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.RequiredArgsConstructor;

/**
 * Classified container provider.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class ClassifiedContainerProvider<K> implements ContainerProvider {
    
    /**
     * container map
     */
    private final Table<K, String, Container<?>> registeredContainers = HashBasedTable.create();

    /**
     * Default key
     */
    private final K defaultKey;

    /**
     * Get data source container by default Key.
     *
     * @param namespace namespace
     * @return container
     * @throws Crane4jException thrown when the container is not registered
     */
    @Override
    public Container<?> getContainer(String namespace) {
        Container<?> container = registeredContainers.get(defaultKey, namespace);
        Assert.notNull(container, () -> new Crane4jException("cannot found container [{}] from locale [{}]", namespace, defaultKey));
        return container;
    }

    /**
     * Register containers for specific locale.
     *
     * @param key locale
     * @param container container
     * @throws Crane4jException thrown when the container is already registered for specific locale
     */
    public void registerContainers(K key, Container<?> container) {
        String namespace = container.getNamespace();
        Assert.isFalse(
            registeredContainers.contains(key, namespace),
            () -> new Crane4jException("container [{}] already registered for locale [{}]", namespace, key)
        );
        registeredContainers.put(key, container.getNamespace(), container);
    }
}
